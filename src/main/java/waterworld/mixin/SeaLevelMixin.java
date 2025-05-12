// src/main/java/waterworld/mixin/SeaLevelMixin.java
package waterworld.mixin;

import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(ChunkGeneratorSettings.class)
public class SeaLevelMixin {
    
    // Static flag to track if we've already logged the change
    private static boolean loggedSeaLevelChange = false;
    
    @Inject(method = "seaLevel", at = @At("RETURN"), cancellable = true)
    private void modifySeaLevel(CallbackInfoReturnable<Integer> cir) {
        int originalSeaLevel = cir.getReturnValue();
        
        // Only log the first time
        if (!loggedSeaLevelChange) {
            ProjectWaterworld.LOGGER.info("Changing sea level from " + originalSeaLevel + " to " + ProjectWaterworld.HIGH_SEA_LEVEL);
            loggedSeaLevelChange = true;
        }
        
        // Always set the sea level to the configured high sea level
        cir.setReturnValue(ProjectWaterworld.HIGH_SEA_LEVEL);
    }
}