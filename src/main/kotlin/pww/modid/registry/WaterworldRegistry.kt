package pww.modid.registry

import org.slf4j.LoggerFactory
import pww.modid.world.WaterworldDatapackSupport

object WaterworldRegistry {
    private val LOGGER = LoggerFactory.getLogger("waterworld:registry")

    fun initialize() {
        LOGGER.info("Initializing Waterworld registry")
        
        // Initialize datapack support
        WaterworldDatapackSupport.initialize()
        
        LOGGER.info("Waterworld registry initialized")
    }
}