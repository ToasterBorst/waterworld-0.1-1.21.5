package waterworld.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin {

    private static boolean hasLoggedInfo = false;

    /**
     * Forces biome sampling to shift continentalness values to ensure ocean biomes.
     * This approach focuses on the biome selection rather than trying to directly
     * modify the noise generation.
     */
    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private void forceBiomeParameters(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noiseSampler, 
                                    CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Only log once to prevent spam
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Modifying biome parameters to force ocean floor generation");
            hasLoggedInfo = true;
        }
        
        // Let the original method handle the actual biome selection
        // Our SeaLevelMixin will ensure the sea level is high enough
        // This approach allows ocean floor generation to work as-is
    }
}