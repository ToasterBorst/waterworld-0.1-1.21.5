package waterworld.mixin;

import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(ChunkNoiseSampler.class)
public class TerrainShapeMixin {

    /**
     * Target the method that creates the final noise values
     */
    @Inject(
        method = "sampleNoiseCorners(IIID)D", 
        at = @At("RETURN"), 
        cancellable = true
    )
    private void modifyTerrainShape(int x, int y, int z, double terrainDensity, CallbackInfoReturnable<Double> cir) {
        double value = cir.getReturnValue();
        
        // Log to verify this is being called
        if (x % 16 == 0 && y % 16 == 0 && z % 16 == 0) {
            ProjectWaterworld.LOGGER.info("TerrainShapeMixin - Noise at y={}: {} -> {}", 
                y, value, modifyValue(value, y));
        }
        
        // Apply our transformation
        cir.setReturnValue(modifyValue(value, y));
    }
    
    /**
     * Helper method to modify values based on height
     */
    private double modifyValue(double value, int y) {
        int seaLevel = 63;
        
        // For high elevations, push values much lower
        if (y > 40) {
            // More aggressive for higher elevations
            double heightFactor = Math.min(2.0, (y - 40) / 20.0);
            
            // Create a scaled transformation that's more negative
            // Value must be negative to generate water/air
            return Math.min(value, -0.7 - heightFactor); 
        }
        
        return value;
    }
}