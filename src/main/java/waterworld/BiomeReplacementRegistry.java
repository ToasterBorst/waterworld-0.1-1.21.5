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
    private static final Map<RegistryKey<Biome>, RegistryEntry<Biome>> REPLACEMENT_ENTRIES = new HashMap<>();
    private static MinecraftServer server;
    
    public static void initialize(MinecraftServer minecraftServer) {
        server = minecraftServer;
        Registry<Biome> biomeRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.BIOME);
        
        ProjectWaterworld.LOGGER.info("Initializing biome replacements...");
        
        // Map ocean biomes to appropriate land biomes
        addReplacement(biomeRegistry, "ocean", "plains");
        addReplacement(biomeRegistry, "deep_ocean", "forest");
        addReplacement(biomeRegistry, "cold_ocean", "taiga");
        addReplacement(biomeRegistry, "deep_cold_ocean", "old_growth_spruce_taiga");
        addReplacement(biomeRegistry, "lukewarm_ocean", "savanna");
        addReplacement(biomeRegistry, "deep_lukewarm_ocean", "jungle");
        addReplacement(biomeRegistry, "warm_ocean", "desert");
        addReplacement(biomeRegistry, "frozen_ocean", "snowy_plains");
        addReplacement(biomeRegistry, "deep_frozen_ocean", "snowy_taiga");
        
        ProjectWaterworld.LOGGER.info("Registered {} biome replacements", REPLACEMENT_ENTRIES.size());
    }
    
    private static void addReplacement(Registry<Biome> registry, String oceanBiome, String landBiome) {
        RegistryKey<Biome> oceanKey = RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", oceanBiome));
        RegistryKey<Biome> landKey = RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", landBiome));
        
        if (registry.contains(oceanKey) && registry.contains(landKey)) {
            // Get the land biome object and find its proper RegistryEntry
            Biome landBiomeObject = registry.get(landKey.getValue());
            if (landBiomeObject != null) {
                int rawId = registry.getRawId(landBiomeObject);
                RegistryEntry.Reference<Biome> landEntry = registry.getEntry(rawId).orElse(null);
                
                // Validate the entry before storing
                if (landEntry != null && landEntry.getKey().isPresent() && landEntry.value() != null) {
                    REPLACEMENT_ENTRIES.put(oceanKey, landEntry);
                    ProjectWaterworld.LOGGER.info("Added replacement: {} -> {} (key: {}, value: {})", 
                        oceanBiome, landBiome, landEntry.getKey().get(), landEntry.value() != null);
                } else {
                    ProjectWaterworld.LOGGER.warn("Failed to create valid entry for replacement {} -> {}", oceanBiome, landBiome);
                }
            }
        } else {
            ProjectWaterworld.LOGGER.warn("Failed to add replacement {} -> {}: biomes not found", oceanBiome, landBiome);
        }
    }
    
    public static RegistryEntry<Biome> getReplacementBiome(RegistryEntry<Biome> originalBiome) {
        if (originalBiome == null || !originalBiome.getKey().isPresent()) {
            return originalBiome;
        }
        
        RegistryKey<Biome> originalKey = originalBiome.getKey().get();
        RegistryEntry<Biome> replacement = REPLACEMENT_ENTRIES.get(originalKey);
        
        return replacement != null ? replacement : originalBiome;
    }
}