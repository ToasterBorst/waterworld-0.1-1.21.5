package pww.modid.mixin;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import pww.modid.WaterWorldConstants;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin {
    
    @Shadow @Final @Mutable private int seaLevel;
    
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void modifySeaLevel(CallbackInfo ci) {
        // Increase sea level to our custom value
        this.seaLevel = WaterWorldConstants.WATER_LEVEL;
    }
}
