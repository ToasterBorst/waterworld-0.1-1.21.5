package waterworld.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import waterworld.ProjectWaterworld;

/**
 * Utility class for ocean biome-related operations
 */
public class OceanBiomeSource {
    private static final Logger LOGGER = LoggerFactory.getLogger("OceanBiomeSource");
    
    /**
     * Checks if a biome is an underground biome that should be preserved
     */
    public static boolean isUndergroundBiome(RegistryEntry<Biome> biome) {
        return biome.getKey().map(key -> {
            String path = key.getValue().getPath();
            return path.contains("cave") || 
                   path.contains("deep_dark") || 
                   path.contains("lush_caves");
        }).orElse(false);
    }
    
    /**
     * Checks if a biome is an ocean biome
     */
    public static boolean isOceanBiome(RegistryEntry<Biome> biome) {
        return biome.getKey().map(key -> 
            key.getValue().getPath().contains("ocean")
        ).orElse(false);
    }
    
    /**
     * Given the y-coordinate, determines if this position should have an ocean biome
     */
    public static boolean shouldBeOcean(int y) {
        return y > 0 && y <= ProjectWaterworld.OCEAN_MAX_Y;
    }
}