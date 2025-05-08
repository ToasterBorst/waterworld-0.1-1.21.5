package pww.modid.mixin;

import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CarvingContext.class)
public class OceanLevelMixin {
    // Modify the getSeaLevel method to return our custom sea level
    @Inject(method = "getSeaLevel", at = @At("HEAD"), cancellable = true)
    private void modifySeaLevel(CallbackInfoReturnable<Integer> cir) {
        // Set sea level to 126 (unchanged from vanilla: 63)
        cir.setReturnValue(126);
    }
}