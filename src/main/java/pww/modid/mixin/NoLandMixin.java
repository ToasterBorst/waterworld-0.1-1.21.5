package pww.modid.mixin;

import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DensityFunction.class)
public interface NoLandMixin {
    // This will be called for all density function calculations
    @Inject(method = "compute", at = @At("RETURN"), cancellable = true)
    default void modifyDensity(DensityFunction.FunctionContext context, CallbackInfoReturnable<Double> cir) {
        // If we're at a Y-level above 111 (sea level - 15), set the density to a negative value
        // to ensure no terrain generates there
        if (context.blockY() > 111) {
            cir.setReturnValue(-1.0);
        }
    }
}