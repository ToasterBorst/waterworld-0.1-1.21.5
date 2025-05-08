package pww.modid

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Waterworld : ModInitializer {
    private val logger = LoggerFactory.getLogger("waterworld")
    
    // Make the water level constant public and const
    const val WATER_LEVEL = 126
    
    // Define the MOD_ID
    const val MOD_ID = "waterworld"

    override fun onInitialize() {
        logger.info("Project Waterworld is initializing!")
        logger.info("Water level set to $WATER_LEVEL")
        logger.info("Waterworld modification active")
    }
}