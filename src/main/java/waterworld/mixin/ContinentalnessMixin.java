package waterworld.mixin;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import waterworld.ProjectWaterworld;

@Mixin(NoiseChunkGenerator.class)
public class ContinentalnessMixin {
    
    // This targets the method that uses continentalness in terrain generation
    @ModifyVariable(method = "sampleDensity", at = @At("HEAD"), argsOnly = true)
    private static double forceLowContinentalness(double value, NoiseRouter noiseRouter) {
        // We can't directly modify continentalness, but we can modify other values
        // that affect overall terrain generation
        return MathHelper.clamp(value, -1.0, -0.7);
    }
}