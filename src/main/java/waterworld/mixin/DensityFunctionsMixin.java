package waterworld.mixin;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

/**
 * A comprehensive approach that targets multiple density function types
 */
@Mixin(targets = {
    "net.minecraft.world.gen.densityfunction.DensityFunctions$ShiftedNoise",
    "net.minecraft.world.gen.densityfunction.DensityFunctions$Noise",
    "net.minecraft.world.gen.densityfunction.DensityFunctions$EndIslands",
    "net.minecraft.world.gen.densityfunction.DensityFunctions$Constant",
    "net.minecraft.world.gen.densityfunction.DensityFunctions$MulOrAdd"
})
public class DensityFunctionsMixin {
    
    private static int counter = 0;
    
    /**
     * Extremely aggressive approach that targets all density functions
     * and forces any that might contribute to terrain height to be deep underwater
     */
    @Inject(method = "compute", at = @At("RETURN"), cancellable = true)
    private void enforceUnderwaterTerrain(DensityFunction.NoisePos pos, CallbackInfoReturnable<Double> cir) {
        double value = cir.getReturnValue();
        
        // Log occasionally to understand what we're catching
        if (counter++ % 10000 == 0) {
            ProjectWaterworld.LOGGER.info("Density function value: {}", value);
        }
        
        // If the value is positive or close to zero, it likely contributes to land
        if (value > -0.3) {
            // Force it deep underwater
            cir.setReturnValue(-0.8);
        }
    }
}