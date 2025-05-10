package waterworld.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;
import waterworld.world.OceanBiomeSource;
import waterworld.util.BiomeHelper;

@Mixin(BiomeAccess.class)
public class OceanHeightBiomeMixin {
    
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void modifyBiomeByHeight(BlockPos pos, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Only apply to Y-levels between 0 and 126
        if (pos.getY() > 0 && pos.getY() <= ProjectWaterworld.HIGH_SEA_LEVEL) {
            RegistryEntry<Biome> originalBiome = cir.getReturnValue();
            
            // Skip if it's already an ocean biome
            if (originalBiome.getKey().isPresent() && 
                BiomeHelper.isOceanBiome(originalBiome.getKey().get().getValue().getPath())) {
                return;
            }
            
            // Skip if it's an underground biome (we want to preserve those)
            if (originalBiome.getKey().isPresent()) {
                String biomePath = originalBiome.getKey().get().getValue().getPath();
                if (biomePath.contains("cave") || 
                    biomePath.contains("deep_dark") || 
                    biomePath.contains("lush_caves")) {
                    return;
                }
            }
            
            // Get the biome registry from the original biome
            if (originalBiome.getKey().isPresent()) {
                // Log occasionally for debugging
                if (Math.random() < 0.0001) {
                    ProjectWaterworld.LOGGER.debug("Replacing " + 
                        originalBiome.getKey().get().getValue() +
                        " with OCEAN at Y=" + pos.getY());
                }
                
                // Replace with OCEAN biome - we'll need to find how to get the registry entry
                // This is tricky without having access to the full registry
                // For now, let's just log the attempt
                ProjectWaterworld.LOGGER.debug("Attempted to replace biome at Y=" + pos.getY());
            }
        }
    }
}