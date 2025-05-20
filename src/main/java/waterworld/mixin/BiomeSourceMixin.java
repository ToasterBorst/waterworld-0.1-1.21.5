package waterworld.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.BiomeReplacementRegistry;
import waterworld.ProjectWaterworld;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import net.minecraft.registry.Registry;
import java.util.Random;

@Mixin(MultiNoiseBiomeSource.class)
public class BiomeSourceMixin {
    private static final boolean DEBUG_BIOME_REPLACEMENT = true;
    private static final boolean LOG_REPLACEMENTS_ONLY = true;
    private static final boolean LOG_OCEAN_CACHING = false;
    private static final boolean LOG_Y_LEVEL = false;
    
    // Constants for biome replacement
    private static final int SEA_LEVEL_BIOME_Y = 31; // y=127 in world coordinates
    private static final int WORLD_SEA_LEVEL = 127; // Actual world sea level
    private static final int REPLACEMENT_START_Y = 126; // Start replacement at y=126
    
    // Biome grid size (similar to vanilla's biome size)
    private static final int BIOME_GRID_SIZE = 16; // Smaller grid for more precise control
    private static final int TRANSITION_RADIUS = 0; // Disable transitions completely
    
    // Spawn chunk radius (in blocks)
    private static final int SPAWN_CHUNK_RADIUS = 128; // 8 chunks radius
    private static final int SPAWN_CHUNK_GRID_RADIUS = SPAWN_CHUNK_RADIUS / BIOME_GRID_SIZE; // Convert to grid units
    
    // Thread-safe cache for biome replacements to ensure consistency
    private static final Map<String, RegistryEntry<Biome>> biomeCache = new ConcurrentHashMap<>();
    
    // Cache for land biome replacements to ensure horizontal consistency
    private static final Map<String, RegistryEntry<Biome>> landBiomeCache = new ConcurrentHashMap<>();
    
    // Cache for ocean types per column - only store once per column
    private static final Map<String, RegistryEntry<Biome>> oceanTypeCache = new ConcurrentHashMap<>();
    
    // Cache for sea level biomes to ensure strict boundary
    private static final Map<String, RegistryEntry<Biome>> seaLevelBiomeCache = new ConcurrentHashMap<>();
    
    // Cache for spawn chunk logging to prevent spam
    private static final Set<String> loggedSpawnBiomes = ConcurrentHashMap.newKeySet();
    
    // Thread-local flag to prevent recursive updates
    private static final ThreadLocal<Boolean> isReplacingBiome = ThreadLocal.withInitial(() -> false);
    
    // Set of ocean biome IDs to ensure we catch all ocean types
    private static final Set<String> OCEAN_BIOMES = new HashSet<>(Arrays.asList(
        "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean",
        "minecraft:cold_ocean", "minecraft:deep_cold_ocean",
        "minecraft:ocean", "minecraft:deep_ocean",
        "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean",
        "minecraft:warm_ocean"
    ));
    
    // Inject earlier in the biome determination process
    @Inject(method = "getBiome(IIILnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;)Lnet/minecraft/registry/entry/RegistryEntry;", at = @At("HEAD"), cancellable = true)
    private void preserveOceanTypes(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler sampler, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Get the column key for this position
        String columnKey = String.format("%d_%d", x, z);
        int worldY = y * 4;
        
        // Check if we're in spawn chunks
        int gridX = x / BIOME_GRID_SIZE;
        int gridZ = z / BIOME_GRID_SIZE;
        
        // Get the current biome - only proceed if we have one
        RegistryEntry<Biome> currentBiome = cir.getReturnValue();
        if (currentBiome == null || !currentBiome.getKey().isPresent()) {
            return;
        }
        
        if (Math.abs(gridX) <= SPAWN_CHUNK_GRID_RADIUS && Math.abs(gridZ) <= SPAWN_CHUNK_GRID_RADIUS) {
            // Log spawn chunk biomes once
            String spawnKey = String.format("%d_%d_%d", x, y, z);
            if (!loggedSpawnBiomes.contains(spawnKey)) {
                loggedSpawnBiomes.add(spawnKey);
                ProjectWaterworld.LOGGER.info("SPAWN CHUNK BIOME: {} at {},{} (world y={})", 
                    currentBiome.getKey().get().getValue(), x, z, worldY);
            }
            return;
        }
        
        // Only preserve deep ocean types below sea level
        if (worldY < WORLD_SEA_LEVEL) {
            RegistryEntry<Biome> cachedOceanType = oceanTypeCache.get(columnKey);
            if (cachedOceanType != null) {
                String biomeId = cachedOceanType.getKey().get().getValue().toString();
                if (biomeId.contains("deep_")) {
                    cir.setReturnValue(cachedOceanType);
                    return;
                }
            }
        }
        
        // Let all other biome determinations happen normally
        return;
    }
    
    // Main biome replacement mixin - simplified to focus on above-sea-level replacement
    @Inject(method = "getBiome(IIILnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;)Lnet/minecraft/registry/entry/RegistryEntry;", at = @At("RETURN"), cancellable = true)
    private void replaceBiomesAboveSeaLevel(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler sampler, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Prevent recursive updates
        if (isReplacingBiome.get()) {
            return;
        }
        
        try {
            isReplacingBiome.set(true);
            
            final RegistryEntry<Biome> currentBiome = cir.getReturnValue();
            if (currentBiome == null || !currentBiome.getKey().isPresent()) {
                return;
            }
            
            String biomeId = currentBiome.getKey().get().getValue().toString();
            int worldY = y * 4; // Convert biome Y to world Y
            
            // Check if this is an ocean biome
            boolean isOcean = OCEAN_BIOMES.contains(biomeId);
            
            // Store ocean type when we first see it at sea level - only once per column
            if (y == SEA_LEVEL_BIOME_Y && isOcean) {
                String columnKey = String.format("%d_%d", x, z);
                if (!oceanTypeCache.containsKey(columnKey)) {
                    oceanTypeCache.put(columnKey, currentBiome);
                    // Log when we store an ocean type - only once per column
                    ProjectWaterworld.LOGGER.info("Stored ocean type {} at {},{} (world y={})", 
                        biomeId, x, z, worldY);
                }
            }
            
            // Handle biome replacement at and above replacement start level
            if (worldY >= REPLACEMENT_START_Y && isOcean) {
                RegistryEntry<Biome> replacementBiome = null;
                
                // For exactly y=126, use exact coordinates
                if (worldY == REPLACEMENT_START_Y) {
                    // Use exact coordinates for sea level to ensure precise boundary
                    String seaLevelKey = String.format("%d_%d", x, z);
                    replacementBiome = seaLevelBiomeCache.get(seaLevelKey);
                    
                    if (replacementBiome == null) {
                        // Get the base ocean type for this column
                        String columnKey = String.format("%d_%d", x, z);
                        RegistryEntry<Biome> baseBiome = oceanTypeCache.getOrDefault(columnKey, currentBiome);
                        
                        // Use a deterministic seed based on coordinates
                        long seed = (long)x * 31 + (long)z * 17;
                        Random random = new Random(seed);
                        
                        // Force a replacement at y=126
                        replacementBiome = BiomeReplacementRegistry.getReplacementBiome(baseBiome, random);
                        
                        if (replacementBiome != null && replacementBiome != baseBiome && 
                            replacementBiome.getKey().isPresent() && replacementBiome.value() != null) {
                            
                            // Cache the sea level biome using exact coordinates
                            seaLevelBiomeCache.put(seaLevelKey, replacementBiome);
                            
                            // Log the replacement
                            ProjectWaterworld.LOGGER.info("SEA LEVEL REPLACEMENT: {} -> {} at {},{} (world y={})", 
                                baseBiome.getKey().get().getValue(),
                                replacementBiome.getKey().get().getValue(),
                                x, z, worldY);
                            
                            // Force the replacement
                            cir.setReturnValue(replacementBiome);
                            return;
                        }
                    } else {
                        // Use cached replacement
                        cir.setReturnValue(replacementBiome);
                        return;
                    }
                } else if (worldY > REPLACEMENT_START_Y) {
                    // For above y=126, use a small grid for consistency
                    int gridX = x / BIOME_GRID_SIZE;
                    int gridZ = z / BIOME_GRID_SIZE;
                    String gridKey = String.format("%d_%d", gridX, gridZ);
                    
                    replacementBiome = landBiomeCache.get(gridKey);
                    
                    if (replacementBiome == null) {
                        // Get the base ocean type for this column
                        String columnKey = String.format("%d_%d", x, z);
                        RegistryEntry<Biome> baseBiome = oceanTypeCache.getOrDefault(columnKey, currentBiome);
                        
                        // Use a deterministic seed based on grid coordinates
                        long seed = (long)gridX * 31 + (long)gridZ * 17;
                        Random random = new Random(seed);
                        
                        replacementBiome = BiomeReplacementRegistry.getReplacementBiome(baseBiome, random);
                        
                        if (replacementBiome != null && replacementBiome != baseBiome && 
                            replacementBiome.getKey().isPresent() && replacementBiome.value() != null) {
                            
                            // Cache the replacement biome
                            landBiomeCache.put(gridKey, replacementBiome);
                            
                            // Log the replacement
                            ProjectWaterworld.LOGGER.info("ABOVE SEA REPLACEMENT: {} -> {} at grid {},{} (world y={})", 
                                baseBiome.getKey().get().getValue(),
                                replacementBiome.getKey().get().getValue(),
                                gridX, gridZ, worldY);
                        } else {
                            replacementBiome = baseBiome;
                        }
                    }
                    
                    if (replacementBiome != currentBiome) {
                        cir.setReturnValue(replacementBiome);
                    }
                }
            }
        } finally {
            isReplacingBiome.set(false);
        }
    }
} 