package waterworld;

// Simple config class for centralized terrain parameters
public class WorldConfig {
    // How much to lower terrain that would be above sea level
    public static final double LAND_LOWERING_FACTOR = 0.5;
    
    // Minimum depth for shallow ocean areas
    public static final int MIN_SHALLOW_DEPTH = 10;
    
    // How much to lower terrain that is already below sea level
    public static final int EXISTING_OCEAN_LOWERING = 40;
    
    // Should we enable debug logging?
    public static final boolean DEBUG_LOGGING = true;  // Enable to see what's happening
}