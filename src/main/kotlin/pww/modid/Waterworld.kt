package pww.modid

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Waterworld : ModInitializer {
    private val logger = LoggerFactory.getLogger("waterworld")
    
    // Define constants
    const val WATER_LEVEL = 126
    const val MOD_ID = "waterworld"

    override fun onInitialize() {
        logger.info("Project Waterworld is initializing!")
        logger.info("Setting sea level to $WATER_LEVEL")
        logger.info("Preventing terrain generation above ${WATER_LEVEL - 15}")
        
        // We'll use mixins for the actual modifications
        logger.info("Waterworld terrain modification active")
    }
}