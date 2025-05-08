package pww.modid.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ExampleMixin {
    @Inject(at = @At("HEAD"), method = "loadLevel")
    private void init(CallbackInfo info) {
        // This code is injected into the start of MinecraftServer.loadLevel()
        if (((MinecraftServer) (Object) this).getPlayerList() != null) {
            ((MinecraftServer) (Object) this).getPlayerList().broadcastSystemMessage(
                Component.literal("§bProject Waterworld is active!"),
                false
            );
        }
    }
}