package pww.modid.world

/**
 * Central location for all constants used in Project Waterworld.
 * This ensures consistency across the codebase.
 */
object WaterworldConstants {
    /** Sea level Y-coordinate for Waterworld */
    const val SEA_LEVEL = 126
    
    /** Maximum terrain height (15 blocks below sea level) */
    const val MAX_TERRAIN_HEIGHT = SEA_LEVEL - 15
    
    /** Mod ID used for registration */
    const val MOD_ID = "waterworld"
}