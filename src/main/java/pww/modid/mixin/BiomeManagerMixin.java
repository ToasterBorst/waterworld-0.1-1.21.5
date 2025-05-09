package pww.modid.mixin;

import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.resources.ResourceKey;

@Mixin(BiomeManager.class)
public class BiomeManagerMixin {
    private static final int SEA_LEVEL = 126;
    private static final int DEBUG_LIMIT = 100; // Limit debug messages to avoid spam
    private static int debugCount = 0;
    
    @Inject(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("RETURN"), cancellable = true)
    private void modifyBiome(BlockPos pos, CallbackInfoReturnable<Holder<Biome>> cir) {
        try {
            // If we're below sea level, check if it's not already an ocean biome
            if (pos.getY() <= SEA_LEVEL && pos.getY() > 50) { // Only check in the upper range where we care most
                ResourceKey<Biome> biomeKey = cir.getReturnValue().unwrapKey().orElse(null);
                if (biomeKey != null && !isOceanBiome(biomeKey) && !isAllowedUndergroundBiome(biomeKey)) {
                    // Log biome information (with limit)
                    if (debugCount < DEBUG_LIMIT) {
                        System.out.println("[Waterworld] Replacing non-ocean biome at y=" + pos.getY() + ": " + biomeKey);
                        debugCount++;
                        
                        if (debugCount == DEBUG_LIMIT) {
                            System.out.println("[Waterworld] Debug limit reached, suppressing further messages");
                        }
                    }
                    
                    // Get the BiomeManager instance
                    BiomeManager manager = (BiomeManager)(Object)this;
                    
                    // Determine which ocean biome to use based on temperature, etc.
                    ResourceKey<Biome> oceanBiome = determineOceanBiome(pos);
                    
                    // Try to get the appropriate ocean biome holder
                    // This is a bit tricky without direct registry access
                    // First, we'll try to find any existing ocean biome in the world
                    try {
                        // Try looking for ocean biomes at this X/Z but at different heights
                        for (int y = SEA_LEVEL; y >= 50; y--) {
                            BlockPos oceanPos = new BlockPos(pos.getX(), y, pos.getZ());
                            Holder<Biome> testBiome = manager.getBiome(oceanPos);
                            ResourceKey<Biome> testKey = testBiome.unwrapKey().orElse(null);
                            
                            if (testKey != null && isOceanBiome(testKey)) {
                                // Found an ocean biome! Use this one.
                                cir.setReturnValue(testBiome);
                                return;
                            }
                        }
                        
                        // If we couldn't find an ocean biome, try to use a default one
                        // This is a fallback mechanism - not ideal but better than nothing
                        if (isTemperatureCold(pos)) {
                            // In colder areas, prefer cold ocean
                            for (int y = SEA_LEVEL; y >= 50; y--) {
                                // Try several different X/Z positions
                                for (int xOffset = -100; xOffset <= 100; xOffset += 50) {
                                    for (int zOffset = -100; zOffset <= 100; zOffset += 50) {
                                        BlockPos testPos = new BlockPos(pos.getX() + xOffset, y, pos.getZ() + zOffset);
                                        Holder<Biome> testBiome = manager.getBiome(testPos);
                                        ResourceKey<Biome> testKey = testBiome.unwrapKey().orElse(null);
                                        
                                        if (testKey != null && isOceanBiome(testKey)) {
                                            // Found an ocean biome! Use this one.
                                            cir.setReturnValue(testBiome);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        
                        // If all else fails, we'll stick with the original biome
                        // Not ideal, but better than crashing
                    } catch (Exception e) {
                        System.out.println("[Waterworld] Error getting ocean biome: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Waterworld] Error in BiomeManagerMixin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper method to determine appropriate ocean biome based on position
    private ResourceKey<Biome> determineOceanBiome(BlockPos pos) {
        // Simple temperature check based on X/Z position
        if (isTemperatureCold(pos)) {
            return Biomes.COLD_OCEAN;
        } else if (isTemperatureWarm(pos)) {
            return Biomes.WARM_OCEAN;
        } else {
            return Biomes.OCEAN;
        }
    }
    
    // Simple temperature check based on coordinates
    private boolean isTemperatureCold(BlockPos pos) {
        // Higher absolute Z values tend to be colder in Minecraft
        return Math.abs(pos.getZ()) > 1000;
    }
    
    // Simple temperature check based on coordinates
    private boolean isTemperatureWarm(BlockPos pos) {
        // Lower absolute Z values tend to be warmer in Minecraft
        return Math.abs(pos.getZ()) < 500;
    }
    
    private static boolean isOceanBiome(ResourceKey<Biome> biomeKey) {
        return biomeKey == Biomes.OCEAN || 
               biomeKey == Biomes.DEEP_OCEAN || 
               biomeKey == Biomes.FROZEN_OCEAN || 
               biomeKey == Biomes.DEEP_FROZEN_OCEAN || 
               biomeKey == Biomes.COLD_OCEAN || 
               biomeKey == Biomes.DEEP_COLD_OCEAN || 
               biomeKey == Biomes.LUKEWARM_OCEAN || 
               biomeKey == Biomes.DEEP_LUKEWARM_OCEAN || 
               biomeKey == Biomes.WARM_OCEAN;
    }
    
    private static boolean isAllowedUndergroundBiome(ResourceKey<Biome> biomeKey) {
        // These biomes are naturally occurring underground or cave biomes that should be preserved
        return biomeKey == Biomes.LUSH_CAVES || 
               biomeKey == Biomes.DRIPSTONE_CAVES ||
               biomeKey == Biomes.DEEP_DARK ||
               biomeKey == Biomes.MANGROVE_SWAMP ||
               biomeKey == Biomes.CHERRY_GROVE || // Some woodlands have underground structures
               biomeKey == Biomes.DARK_FOREST ||
               // Allow mushroom islands for their uniqueness
               biomeKey == Biomes.MUSHROOM_FIELDS ||
               // Add other biomes that should be allowed underground
               biomeKey == Biomes.SWAMP || // Swamps can have underwater features
               biomeKey == Biomes.STONY_SHORE || // Often transitional near oceans
               biomeKey == Biomes.BEACH; // Beaches are transitional to oceans
    }
}