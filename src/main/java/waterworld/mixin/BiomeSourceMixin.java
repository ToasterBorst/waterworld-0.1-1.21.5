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
    private static final boolean LOG_OCEAN_CACHING = true;
    
    // Constants for biome replacement
    private static final int SEA_LEVEL_BIOME_Y = 31; // y=127 in world coordinates
    private static final int WORLD_SEA_LEVEL = 127; // Actual world sea level
    private static final int REPLACEMENT_START_Y = 126; // Start replacement at y=126
    
    // Biome grid size (similar to vanilla's biome size)
    private static final int BIOME_GRID_SIZE = 64; // 4 chunks, matches vanilla biome size
    private static final int TRANSITION_RADIUS = 2; // Affects 2 grid cells in each direction for smoother transitions
    private static final float TRANSITION_FALLOFF = 0.5f; // How quickly the transition effect falls off
    
    // Spawn chunk radius (in blocks)
    private static final int SPAWN_CHUNK_RADIUS = 128; // 8 chunks radius
    private static final int SPAWN_CHUNK_GRID_RADIUS = SPAWN_CHUNK_RADIUS / BIOME_GRID_SIZE; // Convert to grid units
    
    // Thread-safe cache for biome replacements to ensure consistency
    private static final Map<String, RegistryEntry<Biome>> biomeCache = new ConcurrentHashMap<>();
    
    // Cache for land biome replacements to ensure horizontal consistency
    private static final Map<String, RegistryEntry<Biome>> landBiomeCache = new ConcurrentHashMap<>();
    
    // Cache for ocean types per column
    private static final Map<String, RegistryEntry<Biome>> oceanTypeCache = new ConcurrentHashMap<>();
    
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
        
        // If we have a cached ocean type for this column, use it
        RegistryEntry<Biome> cachedOceanType = oceanTypeCache.get(columnKey);
        if (cachedOceanType != null) {
            String biomeId = cachedOceanType.getKey().get().getValue().toString();
            if (biomeId.contains("deep_") && worldY < WORLD_SEA_LEVEL) {
                if (LOG_OCEAN_CACHING) {
                    ProjectWaterworld.LOGGER.info("Using cached deep ocean {} at {}, {}, {} (y={})", 
                        biomeId, x, y, z, worldY);
                }
                cir.setReturnValue(cachedOceanType);
                return;
            }
        }
        
        // If we're at sea level, check if this is a deep ocean
        if (worldY == WORLD_SEA_LEVEL) {
            // Let the original biome determination happen
            return;
        }
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
            int worldY = y * 4;
            
            // Calculate grid position for biome areas
            float gridX = x / (float)BIOME_GRID_SIZE;
            float gridZ = z / (float)BIOME_GRID_SIZE;
            int gridXFloor = (int)Math.floor(gridX);
            int gridZFloor = (int)Math.floor(gridZ);
            
            // Get the grid key for this position
            String gridKey = String.format("%d_%d", gridXFloor, gridZFloor);
            
            // Check if this is an ocean biome
            boolean isOcean = OCEAN_BIOMES.contains(biomeId);
            
            // Store ocean type when we first see it at sea level
            if (y == SEA_LEVEL_BIOME_Y && isOcean) {
                oceanTypeCache.putIfAbsent(gridKey, currentBiome);
                if (LOG_OCEAN_CACHING) {
                    ProjectWaterworld.LOGGER.info("Caching ocean type {} at grid {},{} (world y={})", 
                        biomeId, gridXFloor, gridZFloor, worldY);
                }
            }
            
            // Handle biome replacement at and above replacement start level
            if (worldY >= REPLACEMENT_START_Y && isOcean) {
                // Get or create the land biome for this grid cell
                RegistryEntry<Biome> replacementBiome = landBiomeCache.get(gridKey);
                
                if (replacementBiome == null) {
                    // Get the base ocean type for this grid
                    RegistryEntry<Biome> baseBiome = oceanTypeCache.getOrDefault(gridKey, currentBiome);
                    
                    // Get replacement biome
                    replacementBiome = BiomeReplacementRegistry.getReplacementBiome(baseBiome);
                    
                    if (replacementBiome != null && replacementBiome != baseBiome && 
                        replacementBiome.getKey().isPresent() && replacementBiome.value() != null) {
                        
                        landBiomeCache.put(gridKey, replacementBiome);
                        
                        if (DEBUG_BIOME_REPLACEMENT) {
                            ProjectWaterworld.LOGGER.info("REPLACEMENT: {} -> {} at grid {},{} (world y={})", 
                                baseBiome.getKey().get().getValue(),
                                replacementBiome.getKey().get().getValue(),
                                gridXFloor, gridZFloor, worldY);
                        }
                        
                        // Create smoother transitions by affecting a larger area with falloff
                        for (int dx = -TRANSITION_RADIUS; dx <= TRANSITION_RADIUS; dx++) {
                            for (int dz = -TRANSITION_RADIUS; dz <= TRANSITION_RADIUS; dz++) {
                                float distance = (float)Math.sqrt(dx * dx + dz * dz);
                                if (distance <= TRANSITION_RADIUS) {
                                    // Apply falloff based on distance
                                    float falloff = 1.0f - (distance / TRANSITION_RADIUS) * TRANSITION_FALLOFF;
                                    if (falloff > 0.5f || new Random().nextFloat() < falloff) {
                                        String nearbyKey = String.format("%d_%d", gridXFloor + dx, gridZFloor + dz);
                                        landBiomeCache.putIfAbsent(nearbyKey, replacementBiome);
                                    }
                                }
                            }
                        }
                    } else {
                        replacementBiome = baseBiome;
                    }
                }
                
                if (replacementBiome != currentBiome) {
                    cir.setReturnValue(replacementBiome);
                }
            }
        } finally {
            isReplacingBiome.set(false);
        }
    }
} 