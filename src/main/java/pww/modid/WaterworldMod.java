package pww.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaterworldMod implements ModInitializer {
    public static final String MOD_ID = "waterworld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static final int WATER_LEVEL = 126;

    @Override
    public void onInitialize() {
        LOGGER.info("Project Waterworld is initializing!");
        
        // Register a server starting event to announce the mod is active
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("Setting up Project Waterworld environment...");
        });
        
        LOGGER.info("Project Waterworld initialized successfully!");
    }
}