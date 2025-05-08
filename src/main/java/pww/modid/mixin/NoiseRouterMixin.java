package pww.modid.mixin;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseRouter.class)
public class NoiseRouterMixin {
    private static final int MAX_TERRAIN_HEIGHT = 111; // 15 blocks below our sea level (126)
    private static final int SEA_LEVEL = 126;
    
    // Use the exact method name instead of a wildcard
    @Inject(method = "finalDensity()Lnet/minecraft/world/level/levelgen/DensityFunction;", at = @At("RETURN"), cancellable = true)
    private void limitFinalDensity(CallbackInfoReturnable<DensityFunction> cir) {
        if (cir.getReturnValue() != null) {
            // Cap the terrain height 
            DensityFunction capped = DensityFunctions.min(
                cir.getReturnValue(),
                DensityFunctions.constant(-(SEA_LEVEL - MAX_TERRAIN_HEIGHT)) // Negative values = solid terrain
            );
            cir.setReturnValue(capped);
            
            // Log the change
            System.out.println("[Waterworld] Capped terrain height in NoiseRouter");
        }
    }
}