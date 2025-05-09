package waterworld.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public class BiomeHelper {
    
    /**
     * Determines which ocean biome to use based on the original biome and location
     */
    public static RegistryKey<Biome> getOceanBiomeReplacement(int x, int y, int z) {
        // You can implement biome selection logic here
        // For example, using temperature, humidity, or other factors
        
        // For now, just return regular ocean
        return BiomeKeys.OCEAN;
    }
    
    /**
     * Checks if a biome's registry path contains "ocean"
     */
    public static boolean isOceanBiome(String biomePath) {
        return biomePath != null && biomePath.contains("ocean");
    }
}