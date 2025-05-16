package waterworld;

// Simple config class for centralized terrain parameters
public class WorldConfig {
    // How much to lower terrain that would be above sea level
    public static final double LAND_LOWERING_FACTOR = 0.1;
    
    // Minimum depth for shallow ocean areas
    public static final int MIN_SHALLOW_DEPTH = 3;
    
    // How much to lower terrain that is already below sea level
    public static final int EXISTING_OCEAN_LOWERING = 15;
    
    // Should we enable debug logging?
    public static final boolean DEBUG_LOGGING = false;
}