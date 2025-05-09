package waterworld.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(BiomeAccess.class)
public class OceanHeightBiomeMixin {
    
    // Define biome sets for more efficient lookups
    private static final RegistryKey<Biome> DEFAULT_OCEAN = BiomeKeys.OCEAN;
    
    private static boolean isUndergroundBiome(RegistryEntry<Biome> biome) {
        return biome.getKey().map(key -> {
            String path = key.getValue().getPath();
            return path.contains("cave") || 
                   path.contains("deep_dark") || 
                   path.contains("lush_caves");
        }).orElse(false);
    }
    
    private static boolean isOceanBiome(RegistryEntry<Biome> biome) {
        return biome.getKey().map(key -> 
            key.getValue().getPath().contains("ocean")
        ).orElse(false);
    }
    
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void modifyBiomeByHeight(BlockPos pos, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Only apply to Y-levels between 0 and 126
        if (pos.getY() > 0 && pos.getY() <= ProjectWaterworld.OCEAN_MAX_Y) {
            RegistryEntry<Biome> originalBiome = cir.getReturnValue();
            
            // Skip if it's already an ocean biome
            if (isOceanBiome(originalBiome)) {
                return;
            }
            
            // Skip if it's an underground biome (we want to preserve those)
            if (isUndergroundBiome(originalBiome)) {
                return;
            }
            
            // Get the BiomeAccess instance
            BiomeAccess biomeAccess = (BiomeAccess)(Object)this;
            
            // Here we'd ideally replace the biome with an ocean biome
            // However, we can't directly fetch ocean biomes from the registry in this context
            // We can only log for now
            if (Math.random() < 0.0001) { // Log occasionally to avoid spam
                ProjectWaterworld.LOGGER.debug("Found non-ocean biome at Y=" + pos.getY() + 
                    ": " + originalBiome.getKey().map(k -> k.getValue().toString()).orElse("unknown"));
            }
        }
    }
}