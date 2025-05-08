package pww.modid

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import pww.modid.world.WaterworldTerrainModifier
import org.slf4j.LoggerFactory

object Waterworld : ModInitializer {
    private val logger = LoggerFactory.getLogger("waterworld")
    private const val SEA_LEVEL = 126
    private val MAX_TERRAIN_HEIGHT = WaterworldTerrainModifier.MAX_TERRAIN_HEIGHT

    override fun onInitialize() {
        logger.info("Project Waterworld initializing!")
        logger.info("Setting sea level to $SEA_LEVEL, terrain capped at $MAX_TERRAIN_HEIGHT")
        logger.info("Biome modifications: all terrain below sea level will use ocean biomes")
        
        try {
            // Register a server starting event to verify our changes
            ServerLifecycleEvents.SERVER_STARTING.register { server ->
                logger.info("Waterworld checking world generation settings...")
                try {
                    // Log current sea level to verify our mixin is working
                    val level = server.overworld().seaLevel
                    logger.info("Current sea level: $level (should be $SEA_LEVEL)")
                    logger.info("Terrain height capped at $MAX_TERRAIN_HEIGHT (15 blocks below sea level)")
                    logger.info("Biome modifications active - overriding non-ocean biomes below sea level")
                } catch (e: Exception) {
                    logger.error("Failed to check sea level: ${e.message}")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to register server events: ${e.message}")
        }
    }
}