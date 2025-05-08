package pww.modid.mixin.client;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class BiomeDisplayMixin {
    @Inject(method = "getGameInformation", at = @At("RETURN"))
    private void addBiomeInfo(CallbackInfoReturnable<List<String>> info) {
        if (info.getReturnValue() != null) {
            // Get the current information list
            List<String> list = info.getReturnValue();

            // Add a separator and info message
            list.add("");
            list.add("[Waterworld] Above-water biome detection active");
        }
    }
}