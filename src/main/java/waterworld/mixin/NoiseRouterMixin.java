package waterworld.mixin;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(NoiseRouter.class)
public class NoiseRouterMixin {
    
    /**
     * Target temperature method which returns a double value
     */
    @Inject(
        method = "temperature", 
        at = @At("RETURN"), 
        cancellable = true
    )
    private void modifyTemperature(CallbackInfoReturnable<Double> cir) {
        double originalValue = cir.getReturnValue();
        
        // Log to verify it's working
        ProjectWaterworld.LOGGER.info("Temperature: {}", originalValue);
        
        // We don't modify temperature, just log it to verify our mixin works
        // We'll focus on landSlide which affects terrain height
    }
    
    /**
     * Target finalDensity method which computes the final density value
     */
    @Inject(
        method = "finalDensity", 
        at = @At("RETURN"), 
        cancellable = true
    )
    private void modifyFinalDensity(CallbackInfoReturnable<Double> cir) {
        double originalValue = cir.getReturnValue();
        
        // Log occasionally
        if (Math.random() < 0.0001) {
            ProjectWaterworld.LOGGER.info("Final density: {}", originalValue);
        }
        
        // For values that would create land, force underwater values
        if (originalValue > -0.3) {
            // Negative values = solid blocks, so make it more negative
            double newValue = -0.8;
            
            if (Math.random() < 0.0001) {
                ProjectWaterworld.LOGGER.info("  Modified to: {}", newValue);
            }
            
            cir.setReturnValue(newValue);
        }
    }
}