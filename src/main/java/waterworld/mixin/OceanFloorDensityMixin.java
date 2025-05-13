package waterworld.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(ChunkNoiseSampler.class)
public class OceanFloorDensityMixin {
    
    // This modifies the density parameter directly before it's used
    @ModifyVariable(method = "sampleBlockState", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double modifyDensityForOceanFloor(double density, BlockPos pos, AquiferSampler.FluidLevel fluidLevel) {
        // If we're above our desired max ocean floor height, force density to be negative to generate water
        if (pos.getY() > ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX && density > 0) {
            return -1.0; // Negative density will ensure water generation
        }
        return density;
    }
}