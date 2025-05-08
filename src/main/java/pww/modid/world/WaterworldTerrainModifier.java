package pww.modid.world;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class WaterworldTerrainModifier {
    // Sea level is 126, terrain should max out at 111 (15 blocks below sea level)
    public static final int MAX_TERRAIN_HEIGHT = 111;
    
    // Helper method to cap terrain height
    public static DensityFunction capTerrainHeight(DensityFunction original) {
        return DensityFunctions.min(
            original,
            DensityFunctions.yClampedGradient(
                MAX_TERRAIN_HEIGHT, // Start Y (inclusive)
                MAX_TERRAIN_HEIGHT + 1, // End Y (exclusive)
                1.0, // Value at start Y (solid)
                -1.0 // Value at end Y (air)
            )
        );
    }
}