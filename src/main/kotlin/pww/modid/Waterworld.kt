package pww.modid

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import pww.modid.world.WaterworldBiomeModifications
import pww.modid.world.WaterworldConstants
import org.slf4j.LoggerFactory

/**
 * Project Waterworld - A Minecraft mod that transforms the world into a deep ocean environment.
 * 
 * Key features:
 * - Raises sea level to 126 (from vanilla default of 63)
 * - Caps terrain generation at Y=111 (15 blocks below sea level)
 * - Ensures only ocean biomes generate below sea level (with exceptions for underground biomes)
 * - Preserves normal biome distribution above sea level
 * 
 * This is the main mod class that handles initialization and core functionality.
 */
object Waterworld : ModInitializer {
    private val logger = LoggerFactory.getLogger("waterworld")
    private const val SEA_LEVEL = 126
    private const val MAX_TERRAIN_HEIGHT = 111

    override fun onInitialize() {
        logger.info("Project Waterworld initializing!")
        logger.info("Setting sea level to $SEA_LEVEL, terrain capped at $MAX_TERRAIN_HEIGHT")
        logger.info("Biome modifications: all terrain below sea level will use ocean biomes")
        
        // Initialize biome modifications
        WaterworldBiomeModifications.initialize()
        
        // Register server lifecycle events
        registerServerEvents()
    }
    
    private fun registerServerEvents() {
        try {
            // Use ServerStarted event instead of ServerStarting
            // The ServerStarted event fires after the world is fully loaded
            ServerLifecycleEvents.SERVER_STARTED.register { server ->
                logger.info("Waterworld checking world generation settings...")
                try {
                    // Check if the overworld is available
                    val overworld = server.overworld()
                    if (overworld != null) {
                        // Log current sea level to verify our mixin is working
                        val level = overworld.seaLevel
                        logger.info("Current sea level: $level (expected: $SEA_LEVEL)")
                        
                        if (level != SEA_LEVEL) {
                            logger.warn("Sea level mixin may not be working correctly!")
                        } else {
                            logger.info("Sea level mixin is working correctly")
                        }
                        
                        logger.info("Terrain height capped at $MAX_TERRAIN_HEIGHT (15 blocks below sea level)")
                        logger.info("Biome modifications active - overriding non-ocean biomes below sea level")
                    } else {
                        logger.warn("Overworld not available - can't check sea level")
                    }
                } catch (e: Exception) {
                    logger.error("Failed to check sea level: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to register server events: ${e.message}")
            e.printStackTrace()
        }
    }
}