package waterworld.mixin;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

/**
 * Ultra-focused approach targeting only the critical terrain shape components
 */
@Mixin(targets = {
    // Target the specific terrain-controlling functions
    "net.minecraft.world.gen.densityfunction.DensityFunctions$EndIslandDensityFunction",
    "net.minecraft.world.gen.densityfunction.DensityFunctions$TerrainShaperSpline"
})
public class DensityFunctionsMixin {
    
    @Inject(
        method = "compute(Lnet/minecraft/world/gen/densityfunction/DensityFunction$NoisePos;)D",
        at = @At("RETURN"), 
        cancellable = true
    )
    private void enforceDeepOceanTerrain(DensityFunction.NoisePos pos, CallbackInfoReturnable<Double> cir) {
        double originalValue = cir.getReturnValue();
        int y = pos.blockY();
        
        // Log information about Y position and density values
        if (y % 32 == 0 && pos.blockX() % 32 == 0 && pos.blockZ() % 32 == 0) {
            ProjectWaterworld.LOGGER.info("Terrain density at y={}: original={}", y, originalValue);
        }
        
        // For key terrain-shaping functions, force underwater values
        double newValue;
        
        if (y > 50) {
            // Scale based on height - deeper water for higher elevations
            double depthFactor = Math.min(5.0, (y - 50) / 15.0);
            newValue = -1.5 - depthFactor;
            
            if (y % 32 == 0 && pos.blockX() % 32 == 0 && pos.blockZ() % 32 == 0) {
                ProjectWaterworld.LOGGER.info("  Modified to: {}", newValue);
            }
            
            cir.setReturnValue(newValue);
        }
    }
}