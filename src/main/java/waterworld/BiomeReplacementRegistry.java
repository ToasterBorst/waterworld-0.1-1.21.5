package waterworld;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.server.MinecraftServer;
import java.util.*;
import java.util.function.Function;

public class BiomeReplacementRegistry {
    private static final Map<RegistryKey<Biome>, List<WeightedBiome>> REPLACEMENT_ENTRIES = new HashMap<>();
    private static MinecraftServer server;
    
    private static class WeightedBiome {
        final RegistryEntry<Biome> biome;
        final int weight;
        
        WeightedBiome(RegistryEntry<Biome> biome, int weight) {
            this.biome = biome;
            this.weight = weight;
        }
    }
    
    public static void initialize(MinecraftServer minecraftServer) {
        server = minecraftServer;
        Registry<Biome> biomeRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.BIOME);
        
        ProjectWaterworld.LOGGER.info("Initializing biome replacements...");
        
        // Verify some key biomes exist
        verifyBiomeExists(biomeRegistry, "cold_ocean");
        verifyBiomeExists(biomeRegistry, "plains");
        verifyBiomeExists(biomeRegistry, "forest");
        verifyBiomeExists(biomeRegistry, "cherry_grove");
        verifyBiomeExists(biomeRegistry, "mangrove_swamp");
        
        // Cold biomes (frozen ocean)
        addWeightedReplacements(biomeRegistry, "frozen_ocean", Arrays.asList(
            new WeightedBiomeEntry("snowy_plains", 25),
            new WeightedBiomeEntry("snowy_taiga", 25),
            new WeightedBiomeEntry("snowy_beach", 15),
            new WeightedBiomeEntry("grove", 20),
            new WeightedBiomeEntry("jagged_peaks", 15)
        ));
        
        addWeightedReplacements(biomeRegistry, "deep_frozen_ocean", Arrays.asList(
            new WeightedBiomeEntry("snowy_taiga", 30),
            new WeightedBiomeEntry("old_growth_spruce_taiga", 25),
            new WeightedBiomeEntry("snowy_plains", 20),
            new WeightedBiomeEntry("grove", 15),
            new WeightedBiomeEntry("frozen_peaks", 10)
        ));
        
        // Cold temperate biomes (cold ocean)
        addWeightedReplacements(biomeRegistry, "cold_ocean", Arrays.asList(
            new WeightedBiomeEntry("taiga", 25),
            new WeightedBiomeEntry("old_growth_spruce_taiga", 20),
            new WeightedBiomeEntry("plains", 20),
            new WeightedBiomeEntry("forest", 20),
            new WeightedBiomeEntry("windswept_hills", 15)
        ));
        
        addWeightedReplacements(biomeRegistry, "deep_cold_ocean", Arrays.asList(
            new WeightedBiomeEntry("old_growth_spruce_taiga", 30),
            new WeightedBiomeEntry("taiga", 25),
            new WeightedBiomeEntry("forest", 20),
            new WeightedBiomeEntry("windswept_forest", 15),
            new WeightedBiomeEntry("windswept_gravelly_hills", 10)
        ));
        
        // Temperate biomes (ocean)
        addWeightedReplacements(biomeRegistry, "ocean", Arrays.asList(
            new WeightedBiomeEntry("plains", 20),
            new WeightedBiomeEntry("forest", 20),
            new WeightedBiomeEntry("birch_forest", 15),
            new WeightedBiomeEntry("meadow", 15),
            new WeightedBiomeEntry("beach", 10),
            new WeightedBiomeEntry("cherry_grove", 10),
            new WeightedBiomeEntry("windswept_hills", 10)
        ));
        
        addWeightedReplacements(biomeRegistry, "deep_ocean", Arrays.asList(
            new WeightedBiomeEntry("forest", 25),
            new WeightedBiomeEntry("birch_forest", 20),
            new WeightedBiomeEntry("plains", 15),
            new WeightedBiomeEntry("meadow", 15),
            new WeightedBiomeEntry("cherry_grove", 10),
            new WeightedBiomeEntry("windswept_forest", 10),
            new WeightedBiomeEntry("beach", 5)
        ));
        
        // Warm temperate biomes (lukewarm ocean)
        addWeightedReplacements(biomeRegistry, "lukewarm_ocean", Arrays.asList(
            new WeightedBiomeEntry("savanna", 20),
            new WeightedBiomeEntry("plains", 20),
            new WeightedBiomeEntry("forest", 15),
            new WeightedBiomeEntry("beach", 15),
            new WeightedBiomeEntry("meadow", 10),
            new WeightedBiomeEntry("cherry_grove", 10),
            new WeightedBiomeEntry("swamp", 10)
        ));
        
        addWeightedReplacements(biomeRegistry, "deep_lukewarm_ocean", Arrays.asList(
            new WeightedBiomeEntry("jungle", 25),
            new WeightedBiomeEntry("savanna", 20),
            new WeightedBiomeEntry("forest", 15),
            new WeightedBiomeEntry("mangrove_swamp", 15),
            new WeightedBiomeEntry("swamp", 15),
            new WeightedBiomeEntry("beach", 10)
        ));
        
        // Hot biomes (warm ocean)
        addWeightedReplacements(biomeRegistry, "warm_ocean", Arrays.asList(
            new WeightedBiomeEntry("desert", 25),
            new WeightedBiomeEntry("savanna", 20),
            new WeightedBiomeEntry("beach", 15),
            new WeightedBiomeEntry("plains", 15),
            new WeightedBiomeEntry("cherry_grove", 10),
            new WeightedBiomeEntry("mangrove_swamp", 10),
            new WeightedBiomeEntry("badlands", 5)
        ));
        
        ProjectWaterworld.LOGGER.info("Registered {} biome replacement sets", REPLACEMENT_ENTRIES.size());
        
        // Log all registered replacements
        REPLACEMENT_ENTRIES.forEach((key, replacements) -> {
            ProjectWaterworld.LOGGER.info("Ocean biome {} can be replaced with:", key.getValue());
            replacements.forEach(wb -> {
                if (wb.biome.getKey().isPresent()) {
                    ProjectWaterworld.LOGGER.info("  - {} (weight: {})", 
                        wb.biome.getKey().get().getValue(), wb.weight);
                }
            });
        });
    }
    
    private static void verifyBiomeExists(Registry<Biome> registry, String biomeId) {
        RegistryKey<Biome> key = RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", biomeId));
        if (registry.contains(key)) {
            Biome biome = registry.get(key.getValue());
            if (biome != null) {
                int rawId = registry.getRawId(biome);
                ProjectWaterworld.LOGGER.info("Found biome {} with raw ID {}", biomeId, rawId);
            } else {
                ProjectWaterworld.LOGGER.error("Biome {} exists in registry but is null!", biomeId);
            }
        } else {
            ProjectWaterworld.LOGGER.error("Biome {} not found in registry!", biomeId);
        }
    }
    
    private static class WeightedBiomeEntry {
        final String biomeId;
        final int weight;
        
        WeightedBiomeEntry(String biomeId, int weight) {
            this.biomeId = biomeId;
            this.weight = weight;
        }
    }
    
    private static void addWeightedReplacements(Registry<Biome> registry, String oceanBiome, List<WeightedBiomeEntry> replacements) {
        RegistryKey<Biome> oceanKey = RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", oceanBiome));
        List<WeightedBiome> weightedBiomes = new ArrayList<>();
        
        for (WeightedBiomeEntry entry : replacements) {
            RegistryKey<Biome> landKey = RegistryKey.of(RegistryKeys.BIOME, Identifier.of("minecraft", entry.biomeId));
            
            if (registry.contains(landKey)) {
                Biome landBiomeObject = registry.get(landKey.getValue());
                if (landBiomeObject != null) {
                    int rawId = registry.getRawId(landBiomeObject);
                    RegistryEntry.Reference<Biome> landEntry = registry.getEntry(rawId).orElse(null);
                    
                    if (landEntry != null && landEntry.getKey().isPresent() && landEntry.value() != null) {
                        weightedBiomes.add(new WeightedBiome(landEntry, entry.weight));
                    }
                }
            }
        }
        
        if (!weightedBiomes.isEmpty()) {
            REPLACEMENT_ENTRIES.put(oceanKey, weightedBiomes);
            ProjectWaterworld.LOGGER.info("Added weighted replacements for {} with {} options", oceanBiome, weightedBiomes.size());
        } else {
            ProjectWaterworld.LOGGER.warn("Failed to add weighted replacements for {}", oceanBiome);
        }
    }
    
    public static RegistryEntry<Biome> getReplacementBiome(RegistryEntry<Biome> originalBiome) {
        if (originalBiome == null || !originalBiome.getKey().isPresent()) {
            return originalBiome;
        }
        
        RegistryKey<Biome> originalKey = originalBiome.getKey().get();
        List<WeightedBiome> replacements = REPLACEMENT_ENTRIES.get(originalKey);
        
        if (replacements == null || replacements.isEmpty()) {
            return originalBiome;
        }
        
        // Calculate total weight
        int totalWeight = replacements.stream().mapToInt(wb -> wb.weight).sum();
        
        // Generate random number between 0 and total weight
        int random = new Random().nextInt(totalWeight);
        
        // Find the selected biome based on weights
        int currentWeight = 0;
        for (WeightedBiome weightedBiome : replacements) {
            currentWeight += weightedBiome.weight;
            if (random < currentWeight) {
                return weightedBiome.biome;
            }
        }
        
        // Fallback to first biome if something goes wrong
        return replacements.get(0).biome;
    }
}