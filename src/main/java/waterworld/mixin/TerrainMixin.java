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
        // Log every height calculation attempt
        ProjectWaterworld.LOGGER.info("Height calculation for " + heightmap + " at x:" + x + " z:" + z);

        // For WORLD_SURFACE_WG, which is used for initial terrain generation,
        // we need to ensure it uses ocean floor values
        if (heightmap == Heightmap.Type.WORLD_SURFACE_WG) {
            // Get the height using ocean floor heightmap
            int oceanFloorHeight = ((NoiseChunkGenerator)(Object)this).getHeight(x, z, Heightmap.Type.OCEAN_FLOOR, world, noise);
            // Cap at sea level
            oceanFloorHeight = Math.min(oceanFloorHeight, ProjectWaterworld.HIGH_SEA_LEVEL);
            ProjectWaterworld.LOGGER.info("WORLD_SURFACE_WG using ocean floor height: " + oceanFloorHeight);
            cir.setReturnValue(oceanFloorHeight);
            cir.cancel();
            return;
        }

        // For other non-ocean heightmaps, also use ocean floor
        if (heightmap != Heightmap.Type.OCEAN_FLOOR && heightmap != Heightmap.Type.OCEAN_FLOOR_WG) {
            ProjectWaterworld.LOGGER.info("Non-ocean heightmap used: " + heightmap + " at x:" + x + " z:" + z);
            
            // Get the height using ocean floor heightmap
            int oceanFloorHeight = ((NoiseChunkGenerator)(Object)this).getHeight(x, z, Heightmap.Type.OCEAN_FLOOR, world, noise);
            // Cap at sea level
            oceanFloorHeight = Math.min(oceanFloorHeight, ProjectWaterworld.HIGH_SEA_LEVEL);
            ProjectWaterworld.LOGGER.info("Using ocean floor height: " + oceanFloorHeight + " for " + heightmap);
            
            // Log if we had to cap the height
            if (oceanFloorHeight >= ProjectWaterworld.HIGH_SEA_LEVEL) {
                ProjectWaterworld.LOGGER.info("Capped height at sea level: " + ProjectWaterworld.HIGH_SEA_LEVEL + " at x:" + x + " z:" + z);
            }
            
            cir.setReturnValue(oceanFloorHeight);
            cir.cancel();
        } else {
            // For ocean floor heightmaps, still cap at sea level
            int height = ((NoiseChunkGenerator)(Object)this).getHeight(x, z, heightmap, world, noise);
            height = Math.min(height, ProjectWaterworld.HIGH_SEA_LEVEL);
            ProjectWaterworld.LOGGER.info("Ocean floor heightmap calculation: " + heightmap + " at x:" + x + " z:" + z + " (height: " + height + ")");
            cir.setReturnValue(height);
            cir.cancel();
        }
    }

    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void logFinalHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noise, CallbackInfoReturnable<Integer> cir) {
        int finalHeight = cir.getReturnValue();
        if (finalHeight > ProjectWaterworld.HIGH_SEA_LEVEL) {
            ProjectWaterworld.LOGGER.info("WARNING: Final height above sea level: " + finalHeight + " for " + heightmap + " at x:" + x + " z:" + z);
        }
    }
}