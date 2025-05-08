package pww.modid.mixin;

import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import pww.modid.WaterWorldConstants;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin {
    
    private static final int MAX_TERRAIN_HEIGHT = WaterWorldConstants.WATER_LEVEL - 15;
    
    @Inject(method = "calculateBaseNoise", at = @At("RETURN"), cancellable = true)
    private void limitTerrainHeight(int x, int y, int z, CallbackInfoReturnable<Double> cir) {
        // If we're above the maximum terrain height, force negative density
        // to prevent terrain from generating
        if (y > MAX_TERRAIN_HEIGHT) {
            cir.setReturnValue(-1.0);
        }
    }
}
