package pww.modid.world;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class WaterworldTerrainModifier {
    // Sea level is 126, terrain should max out at 111 (15 blocks below sea level)
    private static final int MAX_TERRAIN_HEIGHT = 111;
    
    // Helper method to cap terrain height
    public static DensityFunction modifyTerrainHeight(DensityFunction original) {
        // Create a function that caps terrain height
        return DensityFunctions.min(
            original,
            DensityFunctions.constant(MAX_TERRAIN_HEIGHT)
        );
    }
}