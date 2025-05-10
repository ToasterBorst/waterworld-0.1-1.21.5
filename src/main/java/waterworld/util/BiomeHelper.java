package waterworld.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class BiomeHelper {
    
    /**
     * Determines which ocean biome to use based on the original biome and location
     */
    public static RegistryKey<Biome> getOceanBiomeReplacement(int x, int y, int z) {
        // Simple noise-based biome selection
        float temperature = getNoise(x * 0.05f, z * 0.05f);
        
        // Use temperature to select ocean biome
        if (temperature < -0.5f) {
            return BiomeKeys.FROZEN_OCEAN;
        } else if (temperature < 0.0f) {
            return BiomeKeys.COLD_OCEAN;
        } else if (temperature > 0.5f) {
            return BiomeKeys.WARM_OCEAN;
        } else {
            return BiomeKeys.OCEAN;
        }
    }
    
    /**
     * Simple noise function for biome selection
     */
    private static float getNoise(float x, float z) {
        return MathHelper.sin(x) * MathHelper.cos(z);
    }
    
    /**
     * Checks if a biome's registry path contains "ocean"
     */
    public static boolean isOceanBiome(String biomePath) {
        return biomePath != null && biomePath.contains("ocean");
    }
}