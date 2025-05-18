package waterworld;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.server.MinecraftServer;
import java.util.HashMap;
import java.util.Map;

public class BiomeReplacementRegistry {
    private static final Map<RegistryKey<Biome>, RegistryKey<Biome>> OCEAN_TO_LAND_REPLACEMENTS = new HashMap<>();
    private static Registry<Biome> biomeRegistry;
    
    public static void initialize(MinecraftServer server) {
        biomeRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.BIOME);
        
        // Map ocean biomes to appropriate land biomes
        addReplacement("ocean", "plains");
        addReplacement("deep_ocean", "forest");
        addReplacement("cold_ocean", "taiga");
        addReplacement("deep_cold_ocean", "old_growth_spruce_taiga");
        addReplacement("lukewarm_ocean", "savanna");
        addReplacement("deep_lukewarm_ocean", "jungle");
        addReplacement("warm_ocean", "desert");
        addReplacement("frozen_ocean", "snowy_plains");
        addReplacement("deep_frozen_ocean", "snowy_taiga");
    }
    
    private static void addReplacement(String oceanBiome, String landBiome) {
        RegistryKey<Biome> oceanKey = RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", oceanBiome));
        RegistryKey<Biome> landKey = RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", landBiome));
        
        if (biomeRegistry.contains(oceanKey) && biomeRegistry.contains(landKey)) {
            OCEAN_TO_LAND_REPLACEMENTS.put(oceanKey, landKey);
        }
    }
    
    public static RegistryEntry<Biome> getReplacementBiome(RegistryEntry<Biome> originalBiome) {
        RegistryKey<Biome> replacementKey = OCEAN_TO_LAND_REPLACEMENTS.get(originalBiome.getKey().orElse(null));
        if (replacementKey != null && biomeRegistry != null) {
            return biomeRegistry.getEntry(replacementKey);
        }
        return originalBiome;
    }
}