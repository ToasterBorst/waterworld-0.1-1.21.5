package waterworld;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ProjectWaterworld implements ModInitializer {
    public static final String MOD_ID = "project-waterworld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // High sea level constant
    public static final int HIGH_SEA_LEVEL = 126;
    
    // The Y level below which all non-underground biomes should be ocean
    public static final int OCEAN_MAX_Y = HIGH_SEA_LEVEL;
    
    // Ocean biome keys for replacing non-ocean biomes
    public static final RegistryKey<Biome>[] OCEAN_BIOMES = new RegistryKey[] {
        BiomeKeys.OCEAN,
        BiomeKeys.DEEP_OCEAN,
        BiomeKeys.COLD_OCEAN,
        BiomeKeys.DEEP_COLD_OCEAN,
        BiomeKeys.FROZEN_OCEAN,
        BiomeKeys.DEEP_FROZEN_OCEAN,
        BiomeKeys.LUKEWARM_OCEAN,
        BiomeKeys.DEEP_LUKEWARM_OCEAN,
        BiomeKeys.WARM_OCEAN
    };
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Project Waterworld with high sea level: " + HIGH_SEA_LEVEL);
        
        // Our SeaLevelMixin will handle raising the sea level
        // Our OceanHeightBiomeMixin will handle biome replacement
    }
}