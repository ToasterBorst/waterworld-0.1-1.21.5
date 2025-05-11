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

@Mixin(NoiseChunkGenerator.class)
public class TerrainMixin {

    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void limitTerrainHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noise, CallbackInfoReturnable<Integer> cir) {
        int originalHeight = cir.getReturnValue();
        
        // Cap terrain height at y=70
        if (originalHeight > 70) {
            cir.setReturnValue(70);
        }
    }
    
    @Inject(method = "populateNoise", at = @At("RETURN"))
    private void flattenHighTerrain(CallbackInfoReturnable<BlockPos> cir) {
        // This method will execute after the chunk is populated with noise
        // We don't need to modify the return value, just injecting at the end
    }
}