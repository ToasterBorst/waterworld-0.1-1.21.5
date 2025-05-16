package waterworld.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import waterworld.ProjectWaterworld;

@Mixin(NoiseChunkGenerator.class)
public class NoiseGeneratorMixin {

    private static boolean hasLoggedTerrainModification = false;

    /**
     * Modify the density values during initial terrain calculation to ensure ocean-appropriate terrain
     */
    @ModifyVariable(method = "populateNoise", at = @At("HEAD"), ordinal = 0)
    private double modifyTerrainDensity(double density) {
        // Log once when our mixin is active
        if (!hasLoggedTerrainModification) {
            ProjectWaterworld.LOGGER.info("Waterworld: Modifying terrain density during generation");
            hasLoggedTerrainModification = true;
        }
        
        // In Minecraft, negative density = solid blocks, positive = air/water
        // If we're calculating density for a position above our desired ocean floor,
        // ensure it stays positive enough to form water/air rather than solid terrain
        return Math.max(density, 0.2);
    }
    
    /**
     * Override height calculations to ensure terrain stays below the ocean floor level
     */
    @ModifyArg(
        method = "getHeight", 
        at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
        index = 0
    )
    private int capTerrainHeight(int originalHeight) {
        // Ensure no terrain rises above our ocean floor height
        return Math.min(originalHeight, ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX);
    }
}