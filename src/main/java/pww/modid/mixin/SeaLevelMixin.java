package pww.modid.mixin;

import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseGeneratorSettings.class)
public class SeaLevelMixin {
    // Intercept all calls that request the sea level value
    @Inject(method = "seaLevel()I", at = @At("HEAD"), cancellable = true)
    private void modifySeaLevel(CallbackInfoReturnable<Integer> cir) {
        // Set sea level to 126
        cir.setReturnValue(126);
    }
    
    // We need to use the exact method signature for overworld settings
    @Inject(method = "overworld(ZZ)Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;", at = @At("RETURN"), cancellable = true)
    private static void modifyOverworldSettings(boolean amplified, boolean large, CallbackInfoReturnable<NoiseGeneratorSettings> cir) {
        System.out.println("[Waterworld] Modifying overworld sea level");
        // We can't manipulate the return value directly in the way we tried before
        // So we'll rely on our other sea level mixin
    }
}