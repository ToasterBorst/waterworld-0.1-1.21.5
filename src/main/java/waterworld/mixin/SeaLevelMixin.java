package waterworld.mixin;

import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(ChunkGeneratorSettings.class)
public class SeaLevelMixin {
    
    @Inject(method = "seaLevel", at = @At("RETURN"), cancellable = true)
    private void modifySeaLevel(CallbackInfoReturnable<Integer> cir) {
        // Always override the sea level with our own high value
        int originalLevel = cir.getReturnValue();
        System.out.println("Waterworld: Changing sea level from " + originalLevel + " to " + ProjectWaterworld.HIGH_SEA_LEVEL);
        cir.setReturnValue(ProjectWaterworld.HIGH_SEA_LEVEL);
    }
}