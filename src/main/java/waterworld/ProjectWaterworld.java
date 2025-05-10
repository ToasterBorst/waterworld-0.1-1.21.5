package waterworld;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectWaterworld implements ModInitializer {
    public static final String MOD_ID = "project-waterworld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // High sea level constant
    public static final int HIGH_SEA_LEVEL = 126;
    
    // The Y level below which all non-underground biomes should be ocean
    public static final int OCEAN_MAX_Y = HIGH_SEA_LEVEL;
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Project Waterworld with high sea level: " + HIGH_SEA_LEVEL);
        
        // For now, we'll rely on mixins only
        // Our SeaLevelMixin will handle raising the sea level
        // Our OceanHeightBiomeMixin will try to handle biome replacement
    }
}