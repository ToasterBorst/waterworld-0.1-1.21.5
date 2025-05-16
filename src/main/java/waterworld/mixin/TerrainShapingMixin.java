package waterworld.mixin;

import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(NoiseChunkGenerator.class)
public class TerrainShapingMixin {

    private static boolean hasLoggedInfo = false;

    /**
     * Modifies terrain density calculations to ensure proper ocean floor generation.
     * This targets the specific method that determines terrain solidity.
     */
    @Inject(method = "sampleDensity", at = @At("RETURN"), cancellable = true)
    private void modifyDensity(int x, int y, int z, double horizontalScale, double verticalScale, 
                              double horizontalStretch, CallbackInfoReturnable<Double> cir) {
        // Log only once to prevent spam
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Modifying terrain density to create proper ocean floors");
            hasLoggedInfo = true;
        }
        
        double originalDensity = cir.getReturnValue();
        
        // Above sea level - ensure it's air (positive density)
        if (y >= ProjectWaterworld.HIGH_SEA_LEVEL) {
            cir.setReturnValue(1.0);
            return;
        }
        
        // Below sea level - create varied ocean floor
        int depthBelowSea = ProjectWaterworld.HIGH_SEA_LEVEL - y;
        
        // Deep underwater - create varied ocean floor with different depth zones
        if (depthBelowSea > 85) {
            // Deep ocean floor - mostly solid with some caves
            double noise = Math.sin(x * 0.05) * Math.cos(z * 0.05) * 0.2;
            cir.setReturnValue(-0.8 + noise);
        } else if (depthBelowSea > 60) {
            // Mid-depth ocean floor - varied terrain with rolling hills
            double noise = Math.sin(x * 0.03) * Math.cos(z * 0.03) * 0.4;
            cir.setReturnValue(-0.6 + noise);
        } else if (depthBelowSea > 40) {
            // Shallow ocean floor - more varied terrain with occasional peaks
            double noise = Math.sin(x * 0.02) * Math.cos(z * 0.02) * 0.5;
            cir.setReturnValue(-0.4 + noise);
        }
        // Leave the upper water column untouched for natural water placement
    }
}