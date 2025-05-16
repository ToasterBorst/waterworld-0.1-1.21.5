package waterworld.mixin;

import java.util.function.Predicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(BiomeSource.class)
public abstract class OceanFloorBiomeMixin {
    
    private static boolean hasLoggedInfo = false;

    /**
     * Intercepts the biome source to force ocean biomes everywhere.
     * This approach specifically ensures that terrain generation matches
     * vanilla ocean floor topology since we're getting actual ocean biomes.
     */
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void forceOceanBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noiseSampler, 
                              CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Only log once to prevent spam
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Forcing ocean biomes and terrain");
            hasLoggedInfo = true;
        }
        
        BiomeSource source = (BiomeSource)(Object)this;
        
        // Get original biome to use for matching temperature ranges
        RegistryEntry<Biome> originalBiome = cir.getReturnValue();
        
        // Find a valid ocean biome based on the current position
        // Use a simple hash of coordinates to deterministically select ocean type
        int hash = Math.abs((x * 13) ^ (z * 7) ^ (y * 91));
        
        // Temperature-based ocean selection (uses the hash to vary ocean types)
        RegistryEntry<Biome> oceanBiome = null;
        
        // Check available biomes in this biome source
        for (RegistryEntry<Biome> biome : source.getBiomes()) {
            if (biome.getKey().isPresent()) {
                if (biome.getKey().get() == BiomeKeys.DEEP_OCEAN || 
                    biome.getKey().get() == BiomeKeys.OCEAN ||
                    biome.getKey().get() == BiomeKeys.DEEP_COLD_OCEAN ||
                    biome.getKey().get() == BiomeKeys.COLD_OCEAN ||
                    biome.getKey().get() == BiomeKeys.DEEP_FROZEN_OCEAN ||
                    biome.getKey().get() == BiomeKeys.FROZEN_OCEAN ||
                    biome.getKey().get() == BiomeKeys.DEEP_LUKEWARM_OCEAN ||
                    biome.getKey().get() == BiomeKeys.LUKEWARM_OCEAN ||
                    biome.getKey().get() == BiomeKeys.WARM_OCEAN) {
                    
                    // If we found an ocean biome, keep track of it
                    // We'll use the first one as a fallback
                    if (oceanBiome == null) {
                        oceanBiome = biome;
                    }
                    
                    // If this is a deep ocean and we're in a valley, prefer it
                    if (y < 40 && biome.getKey().get().toString().contains("deep")) {
                        oceanBiome = biome;
                        break;
                    }
                    
                    // Otherwise if we're above y=40, prefer non-deep oceans
                    if (y >= 40 && !biome.getKey().get().toString().contains("deep")) {
                        oceanBiome = biome;
                    }
                }
            }
        }
        
        // If we found a valid ocean biome, use it
        if (oceanBiome != null) {
            cir.setReturnValue(oceanBiome);
        }
    }
}