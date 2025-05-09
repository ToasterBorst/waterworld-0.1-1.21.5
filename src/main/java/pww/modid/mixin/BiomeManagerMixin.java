package pww.modid.mixin;

import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
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
                    // Log biome information
                    if (debugCount < DEBUG_LIMIT) {
                        System.out.println("[Waterworld] Detected non-ocean biome at y=" + pos.getY() + ": " + biomeKey);
                        
                        // Log information about the BiomeManager instance
                        BiomeManager manager = (BiomeManager)(Object)this;
                        System.out.println("[Waterworld] BiomeManager class: " + manager.getClass().getName());
                        System.out.println("[Waterworld] BiomeManager methods:");
                        for (java.lang.reflect.Method method : manager.getClass().getDeclaredMethods()) {
                            System.out.println("  - " + method.getName() + ": " + method.getReturnType().getName());
                        }
                        
                        // Log information about the biome holder
                        Holder<Biome> biomeHolder = cir.getReturnValue();
                        System.out.println("[Waterworld] Biome holder class: " + biomeHolder.getClass().getName());
                        
                        debugCount++;
                        
                        if (debugCount == DEBUG_LIMIT) {
                            System.out.println("[Waterworld] Debug limit reached, suppressing further messages");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[Waterworld] Error in BiomeManagerMixin: " + e.getMessage());
            e.printStackTrace();
        }
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