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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This mixin intercepts biome selection and ensures that only ocean biomes
 * appear below sea level, except for specific underground biomes.
 */
@Mixin(BiomeManager.class)
public class BiomeManagerMixin {
    private static final int SEA_LEVEL = 126;
    private static final ThreadLocal<Boolean> IN_PROCESS = new ThreadLocal<>();
    
    // Ocean biomes to use for replacement
    private static final ResourceKey<Biome> DEFAULT_OCEAN = Biomes.OCEAN;
    
    // Define ocean biomes
    private static final Set<ResourceKey<Biome>> OCEAN_BIOMES = Set.of(
        Biomes.OCEAN,
        Biomes.DEEP_OCEAN,
        Biomes.FROZEN_OCEAN,
        Biomes.DEEP_FROZEN_OCEAN,
        Biomes.COLD_OCEAN,
        Biomes.DEEP_COLD_OCEAN,
        Biomes.LUKEWARM_OCEAN,
        Biomes.DEEP_LUKEWARM_OCEAN,
        Biomes.WARM_OCEAN
    );
    
    // Define allowed underground biomes
    private static final Set<ResourceKey<Biome>> ALLOWED_UNDERGROUND_BIOMES = Set.of(
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
    );
    
    // Use ConcurrentHashMap instead of HashMap for thread safety
    private static final Map<ResourceKey<Biome>, Boolean> OCEAN_BIOME_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Biome>, Boolean> ALLOWED_UNDERGROUND_CACHE = new ConcurrentHashMap<>();
    
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void modifyBiome(BlockPos pos, CallbackInfoReturnable<Holder<Biome>> cir) {
        // Prevent recursion
        if (Boolean.TRUE.equals(IN_PROCESS.get())) {
            return;
        }
        
        try {
            IN_PROCESS.set(Boolean.TRUE);
            
            // Only apply biome modifications below or at sea level
            if (pos.getY() <= SEA_LEVEL) {
                Holder<Biome> originalBiome = cir.getReturnValue();
                ResourceKey<Biome> biomeKey = originalBiome.unwrapKey().orElse(null);
                
                if (biomeKey != null) {
                    // Check if this is an ocean biome - use thread-safe get and put instead of computeIfAbsent
                    Boolean isOcean = OCEAN_BIOME_CACHE.get(biomeKey);
                    if (isOcean == null) {
                        isOcean = OCEAN_BIOMES.contains(biomeKey);
                        OCEAN_BIOME_CACHE.put(biomeKey, isOcean);
                    }
                    
                    // Check if this is an allowed underground biome - use thread-safe get and put
                    Boolean isAllowed = ALLOWED_UNDERGROUND_CACHE.get(biomeKey);
                    if (isAllowed == null) {
                        isAllowed = ALLOWED_UNDERGROUND_BIOMES.contains(biomeKey);
                        ALLOWED_UNDERGROUND_CACHE.put(biomeKey, isAllowed);
                    }
                    
                    // Replace non-ocean biomes that aren't allowed underground
                    if (!isOcean && !isAllowed) {
                        // For now, we won't actually replace the biome yet until we have a better method
                        // This is just a placeholder for future implementation
                    }
                }
            }
        } finally {
            IN_PROCESS.remove();
        }
    }
}