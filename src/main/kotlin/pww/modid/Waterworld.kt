package pww.modid

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.LoggerFactory

object Waterworld : ModInitializer {
    private val logger = LoggerFactory.getLogger("waterworld")
    private const val SEA_LEVEL = 126
    private const val MAX_TERRAIN_HEIGHT = 111 // 15 blocks below sea level

    override fun onInitialize() {
        logger.info("Project Waterworld initializing!")
        logger.info("Setting sea level to $SEA_LEVEL, terrain capped at $MAX_TERRAIN_HEIGHT")
        
        // Make sure Fabric API is available before registering events
        try {
            // Register a server starting event to verify our changes
            ServerLifecycleEvents.SERVER_STARTING.register { server ->
                logger.info("Waterworld checking world generation settings...")
                // Log current sea level to verify our mixin is working
                val level = server.overworld().seaLevel
                logger.info("Current sea level: $level (should be $SEA_LEVEL)")
            }
        } catch (e: Exception) {
            logger.error("Failed to register server events: ${e.message}")
        }
    }
}