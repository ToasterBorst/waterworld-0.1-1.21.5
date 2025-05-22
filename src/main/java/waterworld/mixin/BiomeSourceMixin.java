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
    private static final boolean DEBUG_BIOME_REPLACEMENT = false;
    private static final boolean LOG_REPLACEMENTS_ONLY = false;
    
    // Constants for biome replacement
    private static final int WORLD_SEA_LEVEL = 127; // Actual world sea level
    private static final int REPLACEMENT_START_Y = 126; // Start replacement at y=126
    
    // Biome grid size (similar to vanilla's biome size)
    private static final int BIOME_GRID_SIZE = 16; // Smaller grid for more precise control
    
    // Cache for land biome replacements to ensure horizontal consistency
    private static final Map<String, RegistryEntry<Biome>> landBiomeCache = new ConcurrentHashMap<>();
    
    // Cache for sea level biomes to ensure strict boundary
    private static final Map<String, RegistryEntry<Biome>> seaLevelBiomeCache = new ConcurrentHashMap<>();
    
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
    
    // Main biome replacement mixin - only handles above-sea-level replacement
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
            
            // Only handle biome replacement at and above replacement start level for ocean biomes
            if (worldY >= REPLACEMENT_START_Y && isOcean) {
                RegistryEntry<Biome> replacementBiome = null;
                
                // For exactly y=126, use exact coordinates
                if (worldY == REPLACEMENT_START_Y) {
                    // Use exact coordinates for sea level to ensure precise boundary
                    String seaLevelKey = String.format("%d_%d", x, z);
                    replacementBiome = seaLevelBiomeCache.get(seaLevelKey);
                    
                    if (replacementBiome == null) {
                        // Use a deterministic seed based on coordinates
                        long seed = (long)x * 31 + (long)z * 17;
                        Random random = new Random(seed);
                        
                        // Force a replacement at y=126
                        replacementBiome = BiomeReplacementRegistry.getReplacementBiome(currentBiome, random);
                        
                        if (replacementBiome != null && replacementBiome != currentBiome && 
                            replacementBiome.getKey().isPresent() && replacementBiome.value() != null) {
                            
                            // Cache the sea level biome using exact coordinates
                            seaLevelBiomeCache.put(seaLevelKey, replacementBiome);
                            
                            // Log the replacement if enabled
                            if (DEBUG_BIOME_REPLACEMENT) {
                                ProjectWaterworld.LOGGER.info("SEA LEVEL REPLACEMENT: {} -> {} at {},{} (world y={})", 
                                    currentBiome.getKey().get().getValue(),
                                    replacementBiome.getKey().get().getValue(),
                                    x, z, worldY);
                            }
                            
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
                        // Use a deterministic seed based on grid coordinates
                        long seed = (long)gridX * 31 + (long)gridZ * 17;
                        Random random = new Random(seed);
                        
                        replacementBiome = BiomeReplacementRegistry.getReplacementBiome(currentBiome, random);
                        
                        if (replacementBiome != null && replacementBiome != currentBiome && 
                            replacementBiome.getKey().isPresent() && replacementBiome.value() != null) {
                            
                            // Cache the replacement biome
                            landBiomeCache.put(gridKey, replacementBiome);
                            
                            // Log the replacement if enabled
                            if (DEBUG_BIOME_REPLACEMENT) {
                                ProjectWaterworld.LOGGER.info("ABOVE SEA REPLACEMENT: {} -> {} at grid {},{} (world y={})", 
                                    currentBiome.getKey().get().getValue(),
                                    replacementBiome.getKey().get().getValue(),
                                    gridX, gridZ, worldY);
                            }
                            
                            cir.setReturnValue(replacementBiome);
                            return;
                        }
                    } else {
                        // Use cached replacement
                        cir.setReturnValue(replacementBiome);
                        return;
                    }
                }
            }
        } finally {
            isReplacingBiome.set(false);
        }
    }
} 