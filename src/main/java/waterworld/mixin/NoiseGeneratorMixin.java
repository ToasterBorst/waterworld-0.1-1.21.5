package waterworld.mixin;

import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import waterworld.ProjectWaterworld;

@Mixin(NoiseChunkGenerator.class)
public class NoiseGeneratorMixin {
    
    private static boolean hasLoggedInfo = false;

    /**
     * Modify the density values to create proper ocean floor topography.
     * This targets the noise value directly, shaping the terrain into
     * a proper varied ocean floor.
     */
    @ModifyVariable(
        method = "populateNoise(Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;",
        at = @At(value = "INVOKE", 
                target = "Lnet/minecraft/world/gen/chunk/ChunkNoiseSampler;sampleNoiseCorners(III)D", 
                ordinal = 0),
        ordinal = 1
    )
    private double modifyTerrainNoise(double originalNoise) {
        // Only log once to avoid spam
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Modifying noise values to create ocean floor topography");
            hasLoggedInfo = true;
        }
        
        // Get the current thread's stack trace to extract position information
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String stackInfo = stackTrace[2].toString(); // Get caller method info
        
        // Generate a hash code from the stack info to create position variation
        int hashCode = stackInfo.hashCode();
        
        // Extract pseudo-coordinates for terrain variation
        int x = hashCode & 0xFFFF;
        int z = (hashCode >> 16) & 0xFFFF;
        
        // Create varied ocean floor terrain using mathematical noise
        double frequency1 = 0.01;
        double frequency2 = 0.02;
        double variation = Math.sin(x * frequency1) * Math.cos(z * frequency1) * 0.3 + 
                          Math.sin(x * frequency2 + 0.5) * Math.cos(z * frequency2 + 0.5) * 0.15;
        
        // Adjust the noise value to create proper ocean floor
        // - Negative values create solid terrain (ocean floor)
        // - Positive values create air/water (ocean)
        // We want to ensure we have a varied underwater terrain
        
        // Create a modified noise value that ensures proper ocean floor topography
        double modifiedNoise = originalNoise;
        
        // If the original noise would create terrain that's too high, 
        // modify it to ensure it stays underwater
        if (originalNoise < 0 && originalNoise > -0.5) {
            // Add variation to create rolling underwater hills
            modifiedNoise = originalNoise + variation;
        }
        
        return modifiedNoise;
    }
}