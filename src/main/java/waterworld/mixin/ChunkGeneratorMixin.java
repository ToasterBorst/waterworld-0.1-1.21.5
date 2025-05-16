package waterworld.mixin;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    private static boolean hasLoggedInfo = false;

    /**
     * Inject at the getHeight method which is used for determining terrain height.
     * This method exists in the ChunkGenerator class (parent of NoiseChunkGenerator)
     * and is more reliable to target.
     */
    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void modifyTerrainHeight(int x, int z, Heightmap.Type heightmap, World world, CallbackInfoReturnable<Integer> cir) {
        // Only log once to prevent spam
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Creating varied ocean floor topography");
            hasLoggedInfo = true;
        }
        
        // Only modify terrain in the overworld
        if (world != null && world.getRegistryKey() == World.OVERWORLD) {
            int originalHeight = cir.getReturnValue();
            
            // Keep any terrain that's already below our minimum height
            // This ensures deep ocean trenches remain intact
            if (originalHeight < 30) {
                return;
            }
            
            // Cap maximum height to ensure everything is underwater
            if (originalHeight >= ProjectWaterworld.HIGH_SEA_LEVEL) {
                cir.setReturnValue(ProjectWaterworld.HIGH_SEA_LEVEL - 1);
                return;
            }
            
            // Create varied ocean floor topography using deterministic noise
            // Use coordinates to create deterministic variation
            double angle = (x * 0.1) + (z * 0.1);
            double variation1 = Math.sin(x * 0.05) * Math.cos(z * 0.05) * 15;
            double variation2 = Math.sin(x * 0.02 + 0.5) * Math.cos(z * 0.02 + 0.5) * 8;
            
            // Base ocean floor level with two layers of noise for natural variation
            int baseLevel = 40;
            int targetHeight = baseLevel + (int)(variation1 + variation2);
            
            // Ensure height stays within a reasonable range for ocean floor
            targetHeight = Math.max(30, Math.min(targetHeight, ProjectWaterworld.HIGH_SEA_LEVEL - 15));
            
            // Return the modified height
            cir.setReturnValue(targetHeight);
        }
    }
}