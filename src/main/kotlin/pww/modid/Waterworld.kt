package pww.modid

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Waterworld : ModInitializer {
    private val logger = LoggerFactory.getLogger("waterworld")
    
    // Define the constants here
    const val WATER_LEVEL = 126
    const val MOD_ID = "waterworld"

    override fun onInitialize() {
        logger.info("Project Waterworld is initializing!")
        logger.info("Water level set to $WATER_LEVEL")
        logger.info("Waterworld modification active")
    }
}