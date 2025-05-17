package waterworld.mixin;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

/**
 * Target the actual terrain shape generator
 */
@Mixin(targets = "net.minecraft.world.gen.densityfunction.DensityFunctions$Shift")
public class ShiftDensityFunctionMixin {
    
    // This specifically targets the Y-shift function which is crucial for terrain height
    @Inject(method = "compute", at = @At("RETURN"), cancellable = true)
    private void modifyTerrainShape(DensityFunction.NoisePos pos, CallbackInfoReturnable<Double> cir) {
        double value = cir.getReturnValue();
        int y = pos.blockY();
        
        // For debugging
        if (y % 32 == 0 && pos.blockX() % 16 == 0 && pos.blockZ() % 16 == 0) {
            ProjectWaterworld.LOGGER.info("Shift density at y={}: original={}", y, value);
        }
        
        // Transform the terrain - push everything down to create ocean floor topology
        if (y > 0) {
            // The higher up, the more we push it down
            double newValue = value - ((y / 16.0) * 0.8);
            
            if (y % 32 == 0 && pos.blockX() % 16 == 0 && pos.blockZ() % 16 == 0) {
                ProjectWaterworld.LOGGER.info("  Modified to: {}", newValue);
            }
            
            cir.setReturnValue(newValue);
        }
    }
}