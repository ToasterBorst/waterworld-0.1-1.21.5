package pww.modid.world

import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.world.level.biome.Biomes
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import org.slf4j.LoggerFactory

/**
 * Handles biome modifications for Project Waterworld using Fabric API.
 */
object WaterworldBiomeModifications {
    private val logger = LoggerFactory.getLogger("waterworld:biomes")
    const val SEA_LEVEL = 126
    
    // Define ocean biomes (public for access from Java)
    @JvmField
    val OCEAN_BIOMES = setOf(
        Biomes.OCEAN,
        Biomes.DEEP_OCEAN,
        Biomes.FROZEN_OCEAN,
        Biomes.DEEP_FROZEN_OCEAN,
        Biomes.COLD_OCEAN,
        Biomes.DEEP_COLD_OCEAN,
        Biomes.LUKEWARM_OCEAN,
        Biomes.DEEP_LUKEWARM_OCEAN,
        Biomes.WARM_OCEAN
    )
    
    // Define allowed underground biomes (public for access from Java)
    @JvmField
    val ALLOWED_UNDERGROUND_BIOMES = setOf(
        Biomes.LUSH_CAVES,
        Biomes.DRIPSTONE_CAVES,
        Biomes.DEEP_DARK,
        Biomes.MANGROVE_SWAMP,
        Biomes.CHERRY_GROVE,
        Biomes.DARK_FOREST,
        Biomes.MUSHROOM_FIELDS,
        Biomes.SWAMP,
        Biomes.STONY_SHORE,
        Biomes.BEACH
    )
    
    fun isOceanBiome(biomeKey: ResourceKey<Biome>): Boolean {
        return biomeKey in OCEAN_BIOMES
    }
    
    fun isAllowedUndergroundBiome(biomeKey: ResourceKey<Biome>): Boolean {
        return biomeKey in ALLOWED_UNDERGROUND_BIOMES
    }
    
    fun initialize() {
        logger.info("Initializing Waterworld biome modifications")
        
        try {
            // For now, let's just focus on logging information about biomes
            logger.info("Biome logging initialized")
            
            // We'll implement the actual modifications once we resolve the ResourceLocation issue
            
        } catch (e: Exception) {
            logger.error("Failed to initialize biome modifications: ${e.message}")
            e.printStackTrace()
        }
    }
}