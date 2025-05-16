package waterworld.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseUtil.class)
public class ContinentalnessOverrideMixin {
    
    // This static field will prevent recursion
    private static final ThreadLocal<Boolean> RECURSION_GUARD = ThreadLocal.withInitial(() -> false);
    
    @Inject(method = "createNoiseValuePoint", at = @At("HEAD"), cancellable = true)
    private static void forceOceanPoint(float temperature, float humidity, float continentalness, 
                                        float erosion, float depth, float weirdness, CallbackInfoReturnable<MultiNoiseUtil.NoiseValuePoint> cir) {
        // Check if we're already inside this method (recursion)
        if (RECURSION_GUARD.get()) {
            // We're recursing, so don't do anything
            return;
        }
        
        try {
            // Set the recursion guard
            RECURSION_GUARD.set(true);
            
            // Only modify continentalness and keep other params the same
            // Force continentalness to ocean value (-1.0)
            if (continentalness > -0.5f) {
                continentalness = -1.0f;
                // Create a new noise point with our forced continentalness value
                MultiNoiseUtil.NoiseValuePoint point = MultiNoiseUtil.createNoiseValuePoint(
                    temperature, humidity, continentalness, erosion, depth, weirdness);
                cir.setReturnValue(point);
            }
        } finally {
            // Always reset the recursion guard
            RECURSION_GUARD.set(false);
        }
    }
}