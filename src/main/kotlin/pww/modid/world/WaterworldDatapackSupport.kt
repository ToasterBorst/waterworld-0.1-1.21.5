package pww.modid.world

import org.slf4j.LoggerFactory

/**
 * Support class for the Waterworld datapack features
 */
object WaterworldDatapackSupport {
    private val LOGGER = LoggerFactory.getLogger("waterworld:datapack")
    
    fun initialize() {
        LOGGER.info("Initializing Waterworld datapack support")
        LOGGER.info("Waterworld preset and noise settings will be loaded from datapacks")
    }
}