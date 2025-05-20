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

@Mixin(MultiNoiseBiomeSource.class)
public class BiomeSourceMixin {
    private static final boolean DEBUG_BIOME_REPLACEMENT = false;
    
    // Constants for biome replacement
    private static final int SEA_LEVEL_BIOME_Y = 31; // y=127 in world coordinates
    private static final int WORLD_SEA_LEVEL = 127; // Actual world sea level
    
    // Biome grid size (similar to vanilla's biome size)
    private static final int BIOME_GRID_SIZE = 64; // Large biome areas for natural transitions
    
    // Spawn chunk radius (in blocks)
    private static final int SPAWN_CHUNK_RADIUS = 128; // 8 chunks radius
    
    // Thread-safe cache for biome replacements to ensure consistency
    private static final Map<String, RegistryEntry<Biome>> biomeCache = new ConcurrentHashMap<>();
    
    // Cache for land biome replacements to ensure horizontal consistency
    private static final Map<String, RegistryEntry<Biome>> landBiomeCache = new ConcurrentHashMap<>();
    
    // Cache for vertical column decisions (whether to replace or not)
    private static final Map<String, Boolean> columnReplacementCache = new ConcurrentHashMap<>();
    
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
        
        // If we have a cached ocean type for this column, use it
        RegistryEntry<Biome> cachedOceanType = oceanTypeCache.get(columnKey);
        if (cachedOceanType != null) {
            // Only use cached type if it's a deep ocean variant
            String biomeId = cachedOceanType.getKey().get().getValue().toString();
            if (biomeId.contains("deep_")) {
                cir.setReturnValue(cachedOceanType);
                return;
            }
        }
        
        // If we're at sea level, check if this is a deep ocean
        int worldY = y * 4;
        if (worldY == WORLD_SEA_LEVEL) {
            // Let the original biome determination happen
            return;
        }
        
        // For positions below sea level, if we have a cached deep ocean type, use it
        if (worldY < WORLD_SEA_LEVEL && cachedOceanType != null) {
            String biomeId = cachedOceanType.getKey().get().getValue().toString();
            if (biomeId.contains("deep_")) {
                cir.setReturnValue(cachedOceanType);
            }
        }
    }
    
    // Main biome replacement mixin
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
            
            // Get the column key for this position
            String columnKey = String.format("%d_%d", x, z);
            
            // Get the grid key for this position
            String gridKey = String.format("%d_%d", gridXFloor, gridZFloor);
            
            // Check if this is an ocean biome
            boolean isOcean = OCEAN_BIOMES.contains(biomeId);
            
            // Store ocean type when we first see it at sea level
            if (y == SEA_LEVEL_BIOME_Y && isOcean) {
                // Only cache deep ocean variants
                if (biomeId.contains("deep_")) {
                    oceanTypeCache.putIfAbsent(columnKey, currentBiome);
                } else {
                    // For shallow oceans, try to find the corresponding deep variant
                    String deepVariant = biomeId.replace("ocean", "deep_ocean")
                                             .replace("cold_ocean", "deep_cold_ocean")
                                             .replace("frozen_ocean", "deep_frozen_ocean")
                                             .replace("lukewarm_ocean", "deep_lukewarm_ocean");
                    
                    // Get the deep variant from the registry
                    RegistryEntry<Biome> deepVariantEntry = ((MultiNoiseBiomeSource)(Object)this).getBiomes().stream()
                        .filter(entry -> entry.getKey().isPresent() && 
                               entry.getKey().get().getValue().toString().equals(deepVariant))
                        .findFirst()
                        .orElse(currentBiome);
                    
                    oceanTypeCache.putIfAbsent(columnKey, deepVariantEntry);
                }
            }
            
            // For ocean biomes below sea level, use the cached deep ocean type
            if (isOcean && worldY < WORLD_SEA_LEVEL) {
                RegistryEntry<Biome> cachedOceanType = oceanTypeCache.get(columnKey);
                if (cachedOceanType != null && cachedOceanType.getKey().isPresent() && 
                    cachedOceanType.getKey().get().getValue().toString().contains("deep_")) {
                    cir.setReturnValue(cachedOceanType);
                    return;
                }
            }
            
            // Handle biome replacement at and above sea level
            if (worldY >= WORLD_SEA_LEVEL) {
                if (isOcean) {
                    // Get or create the land biome for this grid cell
                    String landBiomeKey = String.format("land_%d_%d", gridXFloor, gridZFloor);
                    RegistryEntry<Biome> replacementBiome = landBiomeCache.get(landBiomeKey);
                    
                    if (replacementBiome == null) {
                        // Use the original ocean type for replacement if available
                        RegistryEntry<Biome> baseBiome = oceanTypeCache.get(columnKey);
                        if (baseBiome == null) {
                            baseBiome = currentBiome;
                            // If this is a shallow ocean, try to get its deep variant
                            if (!biomeId.contains("deep_")) {
                                String deepVariant = biomeId.replace("ocean", "deep_ocean")
                                                         .replace("cold_ocean", "deep_cold_ocean")
                                                         .replace("frozen_ocean", "deep_frozen_ocean")
                                                         .replace("lukewarm_ocean", "deep_lukewarm_ocean");
                                
                                RegistryEntry<Biome> deepVariantEntry = ((MultiNoiseBiomeSource)(Object)this).getBiomes().stream()
                                    .filter(entry -> entry.getKey().isPresent() && 
                                           entry.getKey().get().getValue().toString().equals(deepVariant))
                                    .findFirst()
                                    .orElse(currentBiome);
                                
                                baseBiome = deepVariantEntry;
                                oceanTypeCache.putIfAbsent(columnKey, baseBiome);
                            }
                        }
                        
                        replacementBiome = BiomeReplacementRegistry.getReplacementBiome(baseBiome);
                        
                        if (replacementBiome != null && replacementBiome != baseBiome && 
                            replacementBiome.getKey().isPresent() && replacementBiome.value() != null) {
                            
                            landBiomeCache.putIfAbsent(landBiomeKey, replacementBiome);
                            
                            // Create natural transitions by affecting neighboring cells
                            for (int dx = -2; dx <= 2; dx++) {
                                for (int dz = -2; dz <= 2; dz++) {
                                    float distance = (float)Math.sqrt(dx * dx + dz * dz);
                                    if (distance <= 2.0f) {
                                        String nearbyKey = String.format("land_%d_%d", gridXFloor + dx, gridZFloor + dz);
                                        landBiomeCache.putIfAbsent(nearbyKey, replacementBiome);
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
                } else {
                    // No land biomes below sea level
                    RegistryEntry<Biome> cachedOceanType = oceanTypeCache.get(columnKey);
                    if (cachedOceanType != null) {
                        cir.setReturnValue(cachedOceanType);
                    }
                }
            }
        } finally {
            isReplacingBiome.set(false);
        }
    }
} 