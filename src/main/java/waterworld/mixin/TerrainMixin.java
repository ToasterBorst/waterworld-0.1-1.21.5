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

    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void limitTerrainHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noise, CallbackInfoReturnable<Integer> cir) {
        int height = cir.getReturnValue();
        
        // For ocean floor, use vanilla height calculation
        if (heightmap == Heightmap.Type.OCEAN_FLOOR || 
            heightmap == Heightmap.Type.OCEAN_FLOOR_WG) {
            // Ensure ocean floor stays at vanilla depths
            if (height > 64) { // Vanilla ocean floor typically doesn't go above y=64
                cir.setReturnValue(64);
            }
            return;
        }

        // For land features, scale down heights above sea level
        if (height > ProjectWaterworld.HIGH_SEA_LEVEL) {
            // Scale the height while preserving terrain shape
            float scaleFactor = 0.3f; // This will reduce terrain height significantly
            int scaledHeight = ProjectWaterworld.HIGH_SEA_LEVEL + 
                (int)((height - ProjectWaterworld.HIGH_SEA_LEVEL) * scaleFactor);
            cir.setReturnValue(scaledHeight);
        }
    }
}