package waterworld.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(NoiseChunkGenerator.class)
public class OceanTerrainMixin {
    
    private static boolean hasLoggedInfo = false;
    
    // The normal sea level in Minecraft
    private static final int SEA_LEVEL = 63;
    
    // Maximum ocean floor height - anything above this will be flattened
    private static final int MAX_OCEAN_FLOOR_HEIGHT = 45;
    
    /**
     * This mixin intercepts the getHeight method which is used to determine terrain height.
     * We use this to force terrain to stay below sea level, creating ocean floor topology.
     */
    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void limitTerrainHeight(int x, int z, Heightmap.Type heightmap, CallbackInfoReturnable<Integer> cir) {
        // Only log once to prevent spam
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Enforcing ocean floor terrain generation");
            hasLoggedInfo = true;
        }
        
        int originalHeight = cir.getReturnValue();
        
        // If it's already ocean terrain (below sea level), keep it as is
        if (originalHeight < SEA_LEVEL) {
            return;
        }
        
        // Calculate a varied ocean floor height based on coordinates
        // This creates a natural-looking variation in the ocean floor
        double noiseValue = Math.sin(x * 0.05) * Math.cos(z * 0.05) * 10.0;
        int targetHeight = (int)(SEA_LEVEL - 25 + noiseValue);
        
        // Ensure height stays within reasonable ocean floor range
        targetHeight = Math.max(SEA_LEVEL - 40, Math.min(targetHeight, MAX_OCEAN_FLOOR_HEIGHT));
        
        // Return the modified height which will be underwater
        cir.setReturnValue(targetHeight);
    }
}