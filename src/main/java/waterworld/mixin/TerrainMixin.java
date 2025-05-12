package waterworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import waterworld.ProjectWaterworld;

@Mixin(NoiseChunkGenerator.class)
public class TerrainMixin {

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void useOceanFloorHeightmap(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noise, CallbackInfoReturnable<Integer> cir) {
        // For all heightmaps, use the deep ocean floor heightmap calculation
        // This ensures we get true deep ocean floor topography everywhere
        if (heightmap != Heightmap.Type.OCEAN_FLOOR && heightmap != Heightmap.Type.OCEAN_FLOOR_WG) {
            // Get the height using deep ocean floor heightmap
            // Deep ocean floor typically stays below sea level
            int oceanFloorHeight = ((NoiseChunkGenerator)(Object)this).getHeight(x, z, Heightmap.Type.OCEAN_FLOOR, world, noise);
            
            // Ensure we don't get terrain above sea level
            if (oceanFloorHeight > ProjectWaterworld.HIGH_SEA_LEVEL) {
                oceanFloorHeight = ProjectWaterworld.HIGH_SEA_LEVEL;
            }
            
            cir.setReturnValue(oceanFloorHeight);
            cir.cancel();
        }
    }
}