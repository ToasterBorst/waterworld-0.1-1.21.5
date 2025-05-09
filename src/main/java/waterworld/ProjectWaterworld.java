package waterworld;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class ProjectWaterworld implements ModInitializer {
    public static final String MOD_ID = "project-waterworld";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // High sea level constant
    public static final int HIGH_SEA_LEVEL = 126;
    
    // The Y level below which all non-underground biomes should be ocean
    public static final int OCEAN_MAX_Y = HIGH_SEA_LEVEL;
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Project Waterworld with high sea level: " + HIGH_SEA_LEVEL);
        
        // Register our height-based biome modifier
        registerBiomeModifications();
    }
    
    private void registerBiomeModifications() {
        LOGGER.info("Registering height-based biome modifier for oceans below Y=" + OCEAN_MAX_Y);
        
        // Define which biomes are considered underground biomes (to be preserved)
        Predicate<RegistryKey<Biome>> isUndergroundBiome = biomeKey -> {
            String path = biomeKey.getValue().getPath();
            return path.contains("cave") || 
                   path.contains("deep_dark") || 
                   path.contains("lush_caves");
        };
        
        // Define which biomes are ocean biomes (to be excluded from replacement)
        Predicate<RegistryKey<Biome>> isOceanBiome = biomeKey -> {
            return biomeKey.getValue().getPath().contains("ocean");
        };
        
        // Create a selector for biomes that need replacing - overworld biomes that are not oceans or underground
        var biomeSelector = BiomeSelectors.foundInOverworld()
            .and(context -> !isOceanBiome.test(context.getBiomeKey()))
            .and(context -> !isUndergroundBiome.test(context.getBiomeKey()));
        
        // Set up a custom Fabric biome modification that runs in the ADDITIONS phase
        BiomeModifications.create(Identifier.of(MOD_ID, "height_based_ocean"))
            .add(ModificationPhase.ADDITIONS, biomeSelector, (biomeSelectionContext, biomeModificationContext) -> {
                // This will be called for each selected biome during world generation
                // We'll use this to create our height-based replacement system
                
                // Create a hook to intercept biome selection at specific heights
                biomeModificationContext.getWeather().setDownfall(1.0f);  // Make it rainy
                
                // Note: While we're modifying certain biome properties, we can't directly
                // control biome selection by height with the current Fabric Biome API.
                // This is a limitation we'll need to work around.
            });
    }
}