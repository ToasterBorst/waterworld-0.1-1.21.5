package waterworld.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import waterworld.ProjectWaterworld;

@Mixin(ChunkNoiseSampler.class)
public class TerrainShapingMixin {

    // This directly modifies the density value that determines terrain shape
    @ModifyVariable(method = "sampleBlockState", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double modifyDensityValue(double density, BlockPos pos) {
        // If we're at a height where we want the ocean floor, use the original density 
        // to preserve vanilla ocean floor topography
        if (pos.getY() <= ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX) {
            return density;
        }
        
        // For terrain that would normally be above the ocean floor max,
        // force negative density so it becomes water instead of solid blocks
        return -0.1;
    }
}