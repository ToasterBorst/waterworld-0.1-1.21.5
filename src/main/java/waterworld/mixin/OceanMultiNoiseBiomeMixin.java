package waterworld.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;
import waterworld.world.OceanBiomeSource;

@Mixin(MultiNoiseBiomeSource.class)
public class OceanMultiNoiseBiomeMixin {
    
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void modifyBiomeSelection(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noiseSampler, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Only apply in the overworld - there's no direct way to check dimension, 
        // but we can infer based on the biome source being used
        
        // Apply our ocean biome rules when Y <= 126 and Y > 0
        if (OceanBiomeSource.shouldBeOcean(y)) {
            RegistryEntry<Biome> originalBiome = cir.getReturnValue();
            
            if (originalBiome.getKey().isPresent()) {
                RegistryKey<Biome> biomeKey = originalBiome.getKey().get();
                
                // Skip if already an ocean biome
                if (OceanBiomeSource.isOceanBiome(originalBiome)) {
                    return;
                }
                
                // Skip if it's an underground biome (to preserve those)
                if (OceanBiomeSource.isUndergroundBiome(originalBiome)) {
                    return;
                }
                
                // For debugging
                if (Math.random() < 0.0001) {
                    ProjectWaterworld.LOGGER.debug("MultiNoiseBiomeSource found non-ocean biome at y=" + y + 
                            ": " + biomeKey.getValue().toString());
                }
            }
        }
    }
}
