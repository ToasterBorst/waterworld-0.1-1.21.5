package waterworld.mixin;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;
import java.util.function.Predicate;

@Mixin(NoiseChunkGenerator.class)
public class NoiseChunkGeneratorMixin {

    // Constants for our oceans
    private static final int SEA_LEVEL = 63;
    
    // Target the sampleHeightmap method
    @Inject(method = "sampleHeightmap", at = @At("RETURN"), cancellable = true)
    private void modifyHeightmap(HeightLimitView heightLimitView, NoiseConfig noiseConfig, 
                               int x, int z, MutableObject<?> mutableObject, 
                               Predicate<?> predicate, CallbackInfoReturnable<OptionalInt> cir) {
        
        // Get the OptionalInt value from the result
        OptionalInt optHeight = cir.getReturnValue();
        
        // Only modify if there's actually a height value present
        if (optHeight.isPresent()) {
            // Original height - use this to maintain relative terrain features
            int height = optHeight.getAsInt();
            
            // Even more aggressive height transformation - everything goes deep underwater
            // Force all terrain to be at least 30 blocks below sea level
            int oceanHeight = SEA_LEVEL - 30;
            
            // Return the modified height
            cir.setReturnValue(OptionalInt.of(oceanHeight));
        }
    }
    
    // Target the getHeight method for consistency
    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void modifyHeight(int x, int z, net.minecraft.world.Heightmap.Type heightmap, 
                            HeightLimitView world, NoiseConfig noiseConfig, 
                            CallbackInfoReturnable<Integer> cir) {
        
        // Same fixed ocean depth for consistency
        int oceanHeight = SEA_LEVEL - 30;
        
        cir.setReturnValue(oceanHeight);
    }
}