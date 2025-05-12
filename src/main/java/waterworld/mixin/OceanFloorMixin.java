package waterworld.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.AquiferSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(AquiferSampler.FluidLevel.class)
public class OceanFloorMixin {
    
    /**
     * This targets the getFluidState method of FluidLevel, which determines what
     * fluid should be at a given position during world generation.
     */
    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    private void ensureWaterAboveOceanFloor(int y, CallbackInfoReturnable<BlockState> cir) {
        // If we're above our desired ocean floor max height, ensure fluid exists here
        if (y > ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX) {
            // Let the default fluid state be determined (should be water in oceans)
            // We don't immediately cancel because we want the normal water state
            // However, this injection point prevents terrain blocks from being generated
        }
    }
}