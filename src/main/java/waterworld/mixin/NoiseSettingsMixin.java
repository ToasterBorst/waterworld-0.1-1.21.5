package waterworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import waterworld.ProjectWaterworld;

@Mixin(ChunkGeneratorSettings.class)
public class NoiseSettingsMixin {
    
    // This forces all continentalness values to be very negative,
    // which should result in only ocean terrain generation
    @Inject(method = "getContinentalnessNoise", at = @At("RETURN"), cancellable = true)
    private void forceLowContinentalness(CallbackInfoReturnable<Double> cir) {
        // Extremely negative value ensures oceans
        cir.setReturnValue(-1.5); 
    }
    
    // Override height multiplier to create flatter terrain
    @Inject(method = "terrainAmplifier", at = @At("RETURN"), cancellable = true)
    private void flattenTerrain(CallbackInfoReturnable<Double> cir) {
        // A lower value creates flatter terrain
        cir.setReturnValue(0.5);
    }
}