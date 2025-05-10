package waterworld.mixin;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;
import waterworld.world.OceanBiomeSource;
import waterworld.util.BiomeHelper;

@Mixin(BiomeAccess.class)
public class OceanHeightBiomeMixin {
    
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void modifyBiomeByHeight(BlockPos pos, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Only apply to Y-levels between 0 and 126
        if (OceanBiomeSource.shouldBeOcean(pos.getY())) {
            RegistryEntry<Biome> originalBiome = cir.getReturnValue();
            
            // Skip if it's already an ocean biome
            if (OceanBiomeSource.isOceanBiome(originalBiome)) {
                return;
            }
            
            // Skip if it's an underground biome (we want to preserve those)
            if (OceanBiomeSource.isUndergroundBiome(originalBiome)) {
                return;
            }
            
            // Attempt to get an appropriate ocean biome based on the original biome's location
            RegistryKey<Biome> oceanBiomeKey = BiomeHelper.getOceanBiomeReplacement(pos.getX(), pos.getY(), pos.getZ());
            
            // Log debug only occasionally to avoid excessive logging
            if (Math.random() < 0.0001) {
                ProjectWaterworld.LOGGER.debug("Replacing " + 
                    originalBiome.getKey().map(k -> k.getValue().toString()).orElse("unknown") +
                    " with " + oceanBiomeKey.getValue() + " at Y=" + pos.getY());
            }
            
            // Get the biome registry and the ocean biome
            BiomeAccess biomeAccess = (BiomeAccess)(Object)this;
            
            // Get the ocean biome from the registry
            // In the case we can't get the ocean biome, we'll keep the original
            // But we need to implement a way to get the ocean biome entry
        }
    }
}