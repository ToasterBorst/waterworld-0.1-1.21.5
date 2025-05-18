package waterworld;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectWaterworld implements ModInitializer {
    public static final String MOD_ID = "project-waterworld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Project Waterworld");
        
        // Register embedded datapack
        try {
            Identifier packId = Identifier.of(MOD_ID, "waterworld");
            
            ResourceManagerHelper.registerBuiltinResourcePack(
                packId, 
                FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),
                ResourcePackActivationType.DEFAULT_ENABLED
            );
            
            LOGGER.info("Registered Waterworld datapack");
        } catch (Exception e) {
            LOGGER.error("Failed to register datapack: " + e.getMessage());
        }
        
        // Initialize biome replacement registry when server starts
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            BiomeReplacementRegistry.initialize(server);
        });
    }
}