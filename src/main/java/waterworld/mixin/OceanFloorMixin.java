package waterworld.mixin;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseUtil.class)
public class OceanFloorMixin {
    
    /**
     * This targets the createNoiseValuePoint method which is used to create the
     * noise point that determines terrain features including continentalness.
     */
    @Inject(method = "createNoiseValuePoint", at = @At("RETURN"), cancellable = true)
    private static void modifyContinentalness(float temperature, float humidity, float continentalness, 
                                             float erosion, float depth, float weirdness, 
                                             CallbackInfoReturnable<MultiNoiseUtil.NoiseValuePoint> cir) {
        
        // Replace the returned noise point with one that has a very negative continentalness
        // but keeps all other parameters the same
        MultiNoiseUtil.NoiseValuePoint original = cir.getReturnValue();
        
        // Create a new noise point with forced low continentalness
        MultiNoiseUtil.NoiseValuePoint modified = MultiNoiseUtil.createNoiseValuePoint(
            temperature,       // Keep original temperature
            humidity,          // Keep original humidity
            -1.5f,             // Force very negative continentalness (ocean terrain)
            erosion,           // Keep original erosion
            depth,             // Keep original depth
            weirdness          // Keep original weirdness
        );
        
        cir.setReturnValue(modified);
    }
}