package waterworld.mixin;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.Heightmap;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

import java.util.OptionalInt;
import java.util.function.Predicate;

@Mixin(NoiseChunkGenerator.class)
public class OceanFloorMixin {
    
    // Keep the getHeight method injection
    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void limitTerrainHeight(int x, int z, Heightmap.Type heightmap, 
                                   HeightLimitView heightLimitView, 
                                   NoiseConfig noiseConfig,
                                   CallbackInfoReturnable<Integer> cir) {
        // Always cap terrain to our maximum
        cir.setReturnValue(Math.min(cir.getReturnValue(), ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX));
    }
    
    // Fix the sampleHeightmap injection to handle OptionalInt correctly
    @Inject(method = "sampleHeightmap", at = @At("RETURN"), cancellable = true)
    private void limitHeightmapSample(HeightLimitView heightLimitView, NoiseConfig noiseConfig, 
                                     int x, int z, 
                                     MutableObject<?> mutableObject, 
                                     Predicate<?> predicate,
                                     CallbackInfoReturnable<OptionalInt> cir) {
        // Now correctly handle OptionalInt
        OptionalInt originalHeight = cir.getReturnValue();
        
        // Only process if there is a value present
        if (originalHeight.isPresent()) {
            int height = originalHeight.getAsInt();
            if (height > ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX) {
                // Replace with our capped height
                cir.setReturnValue(OptionalInt.of(ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX));
            }
        }
    }
}