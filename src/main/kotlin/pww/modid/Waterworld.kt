package pww.modid

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import pww.modid.world.WaterworldConstants
import org.slf4j.LoggerFactory

/**
 * Project Waterworld - A Minecraft mod that transforms the world into a deep ocean environment.
 */
object Waterworld : ModInitializer {
    private val logger = LoggerFactory.getLogger("waterworld")

    override fun onInitialize() {
        logger.info("Project Waterworld initializing!")
        logger.info("Setting sea level to ${WaterworldConstants.SEA_LEVEL}")
        logger.info("Terrain capped at ${WaterworldConstants.MAX_TERRAIN_HEIGHT}")
        
        // Register server lifecycle events to verify world generation
        registerServerEvents()
    }
    
    private fun registerServerEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            logger.info("Waterworld checking world generation settings...")
            try {
                val overworld = server.overworld()
                if (overworld != null) {
                    val level = overworld.seaLevel
                    logger.info("Current sea level: $level (expected: ${WaterworldConstants.SEA_LEVEL})")
                    
                    if (level != WaterworldConstants.SEA_LEVEL) {
                        logger.warn("Sea level may not be set correctly")
                    } else {
                        logger.info("Sea level is set correctly")
                    }
                } else {
                    logger.warn("Overworld not available - can't check sea level")
                }
            } catch (e: Exception) {
                logger.error("Failed to check sea level: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}