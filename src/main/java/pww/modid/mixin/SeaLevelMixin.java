package pww.modid.mixin;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseGeneratorSettings.class)
public class SeaLevelMixin {
    // Hardcoded value to prevent path issues
    private static final int SEA_LEVEL = 126;
    
    // Intercept all calls that request the sea level value
    @Inject(method = "seaLevel", at = @At("HEAD"), cancellable = true)
    private void modifySeaLevel(CallbackInfoReturnable<Integer> cir) {
        // Set sea level to 126
        cir.setReturnValue(SEA_LEVEL);
    }
}