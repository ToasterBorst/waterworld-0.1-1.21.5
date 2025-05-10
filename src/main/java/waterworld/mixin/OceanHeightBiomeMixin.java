package waterworld.mixin;

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
            
            // For now, just log that we found a non-ocean biome at this height
            if (Math.random() < 0.0001) { // Log occasionally to avoid spam
                ProjectWaterworld.LOGGER.debug("Found non-ocean biome at Y=" + pos.getY() + 
                    ": " + originalBiome.getKey().map(k -> k.getValue().toString()).orElse("unknown"));
            }
        }
    }
}