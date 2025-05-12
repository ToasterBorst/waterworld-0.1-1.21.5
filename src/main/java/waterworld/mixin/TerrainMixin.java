// src/main/java/waterworld/mixin/TerrainMixin.java
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

    // Track if we're already inside our mixin to prevent recursion
    private static final ThreadLocal<Boolean> PROCESSING = ThreadLocal.withInitial(() -> false);
    
    private static boolean loggedStartup = false;

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void redirectToOceanFloor(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noise, CallbackInfoReturnable<Integer> cir) {
        if (!loggedStartup) {
            ProjectWaterworld.LOGGER.info("WaterWorld heightmap redirection active - redirecting all surface heightmaps to ocean floor");
            loggedStartup = true;
        }
        
        // Skip if we're already inside our mixin to prevent recursion
        if (PROCESSING.get()) {
            return;
        }
        
        // Only redirect WORLD_SURFACE and MOTION_BLOCKING heightmaps
        // This is where terrain placement decisions are made
        if (heightmap == Heightmap.Type.WORLD_SURFACE || 
            heightmap == Heightmap.Type.WORLD_SURFACE_WG || 
            heightmap == Heightmap.Type.MOTION_BLOCKING || 
            heightmap == Heightmap.Type.MOTION_BLOCKING_NO_LEAVES) {
            
            try {
                PROCESSING.set(true);
                
                // Map the surface heightmap to the appropriate ocean floor version
                Heightmap.Type oceanType = (heightmap == Heightmap.Type.WORLD_SURFACE_WG) ? 
                                          Heightmap.Type.OCEAN_FLOOR_WG : 
                                          Heightmap.Type.OCEAN_FLOOR;
                
                // Get the height using ocean floor heightmap
                NoiseChunkGenerator generator = (NoiseChunkGenerator)(Object)this;
                int height = generator.getHeight(x, z, oceanType, world, noise);
                
                // Log occasionally for debugging
                if (Math.random() < 0.0001) {
                    ProjectWaterworld.LOGGER.info("Using " + oceanType + " instead of " + heightmap + 
                                 " at x:" + x + " z:" + z + " (height: " + height + ")");
                }
                
                cir.setReturnValue(height);
                cir.cancel();
            } finally {
                PROCESSING.set(false);
            }
        }
    }
}