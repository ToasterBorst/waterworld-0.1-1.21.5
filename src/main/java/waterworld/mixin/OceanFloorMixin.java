package waterworld.mixin;

import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.Heightmap;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(NoiseChunkGenerator.class)
public class OceanFloorMixin {
    
    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void limitTerrainHeight(int x, int z, Heightmap.Type heightmap, 
                                   HeightLimitView heightLimitView, 
                                   NoiseConfig noiseConfig,
                                   CallbackInfoReturnable<Integer> cir) {
        // Cap terrain height to our maximum ocean floor level
        int currentHeight = cir.getReturnValue();
        if (currentHeight > ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX) {
            cir.setReturnValue(ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX);
        }
    }
}