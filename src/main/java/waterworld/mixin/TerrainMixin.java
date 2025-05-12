package waterworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.Heightmap;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import waterworld.ProjectWaterworld;

@Mixin(NoiseChunkGenerator.class)
public class TerrainMixin {
    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void enforceOceanFloor(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noise, CallbackInfoReturnable<Integer> cir) {
        // Get the original height
        int originalHeight = cir.getReturnValue();
        
        // For SURFACE heightmaps, force height to be at most 40 (well below sea level)
        // This is a very aggressive limit to ensure vanilla ocean topography
        if (heightmap == Heightmap.Type.WORLD_SURFACE || 
            heightmap == Heightmap.Type.WORLD_SURFACE_WG || 
            heightmap == Heightmap.Type.MOTION_BLOCKING || 
            heightmap == Heightmap.Type.MOTION_BLOCKING_NO_LEAVES) {
            
            int cappedHeight = Math.min(originalHeight, 40);
            cir.setReturnValue(cappedHeight);
        }
    }
}