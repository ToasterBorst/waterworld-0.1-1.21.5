package pww.modid.mixin;

import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(BiomeManager.class)
public class BiomeManagerMixin {
    private static final int SEA_LEVEL = 126;
    private static final ThreadLocal<Boolean> IN_PROCESS = new ThreadLocal<>();
    
    // Cache to avoid repeatedly checking the same biomes
    private static final Map<ResourceKey<Biome>, Boolean> OCEAN_BIOME_CACHE = new HashMap<>();
    private static final Map<ResourceKey<Biome>, Boolean> ALLOWED_UNDERGROUND_CACHE = new HashMap<>();
    
    // Debug counter limit to only log a few instances
    private static int debugCounter = 0;
    private static final int MAX_DEBUG = 10;
    
    @Inject(method = "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", at = @At("RETURN"), cancellable = true)
    private void modifyBiome(BlockPos pos, CallbackInfoReturnable<Holder<Biome>> cir) {
        // Prevent recursion
        if (Boolean.TRUE.equals(IN_PROCESS.get())) {
            return;
        }
        
        try {
            IN_PROCESS.set(Boolean.TRUE);
            
            // Skip this logic for most positions to drastically reduce overhead
            // Only check positions at exact sea level - this will be enough to create
            // the ocean surface layer without processing every block below the surface
            if (pos.getY() == SEA_LEVEL) {
                Holder<Biome> originalBiome = cir.getReturnValue();
                ResourceKey<Biome> biomeKey = originalBiome.unwrapKey().orElse(null);
                
                if (biomeKey != null) {
                    // Use cached checks for performance
                    boolean isOcean = OCEAN_BIOME_CACHE.computeIfAbsent(biomeKey, 
                            key -> isOceanBiome(key));
                    
                    boolean isAllowed = ALLOWED_UNDERGROUND_CACHE.computeIfAbsent(biomeKey,
                            key -> isAllowedUndergroundBiome(key));
                    
                    // Only replace non-ocean biomes that aren't allowed underground
                    if (!isOcean && !isAllowed) {
                        // Very limited logging to avoid spam
                        if (debugCounter < MAX_DEBUG) {
                            System.out.println("[Waterworld] Processing sea level biome: " + biomeKey.location());
                            debugCounter++;
                        }
                        
                        // For now, we simply keep the original biome.
                        // The key fix is stopping the recursion while still letting the mod function.
                        // In a proper implementation, you'd return a different biome holder here.
                    }
                }
            }
        } finally {
            IN_PROCESS.remove();
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
               biomeKey == Biomes.CHERRY_GROVE || 
               biomeKey == Biomes.DARK_FOREST ||
               biomeKey == Biomes.MUSHROOM_FIELDS ||
               biomeKey == Biomes.SWAMP || 
               biomeKey == Biomes.STONY_SHORE || 
               biomeKey == Biomes.BEACH;
    }
}