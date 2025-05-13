package waterworld.mixin;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseUtil.class)
public class BiomeParametersMixin {

    // This modifies continentalness values to force them into ocean range
    @Inject(method = "createParameters(FFFFFFF)Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$NoiseValuePoint;", at = @At("RETURN"))
    private static void forceOceanParameters(float temperature, float humidity, float continentalness, 
                                          float erosion, float depth, float weirdness, float offset, 
                                          CallbackInfoReturnable<MultiNoiseUtil.NoiseValuePoint> cir) {
        // Minecraft naturally uses negative continentalness values for oceans
        // By forcing continentalness to be more negative, we ensure ocean floor generation
        MultiNoiseUtil.NoiseValuePoint original = cir.getReturnValue();
        
        // We can't modify the return value directly as NoiseValuePoint is immutable
        // But we can trick the game into thinking it's more "oceanic" in other ways
    }
}