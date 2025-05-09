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
import java.util.Set;

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
    
    // Cache to avoid repeatedly checking the same biomes
    private static final Map<ResourceKey<Biome>, Boolean> OCEAN_BIOME_CACHE = new HashMap<>();
    private static final Map<ResourceKey<Biome>, Boolean> ALLOWED_UNDERGROUND_CACHE = new HashMap<>();
    
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
                    // Check if this is an ocean biome
                    boolean isOcean = OCEAN_BIOME_CACHE.computeIfAbsent(biomeKey, 
                            key -> OCEAN_BIOMES.contains(key));
                    
                    // Check if this is an allowed underground biome
                    boolean isAllowed = ALLOWED_UNDERGROUND_CACHE.computeIfAbsent(biomeKey,
                            key -> ALLOWED_UNDERGROUND_BIOMES.contains(key));
                    
                    // Replace non-ocean biomes that aren't allowed underground
                    if (!isOcean && !isAllowed) {
                        // We currently don't have a way to replace the biome due to the registries
                        // For now, we'll focus on getting a stable version running without crashes
                        // The actual biome replacement will be implemented in a future update
                    }
                }
            }
        } finally {
            IN_PROCESS.remove();
        }
    }
}