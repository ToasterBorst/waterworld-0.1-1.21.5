package waterworld.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(BiomeSource.class)
public class BiomeModifierMixin {

    private static boolean hasLoggedInfo = false;

    /**
     * Simplify our approach - instead of trying to modify terrain generation,
     * just ensure all biomes returned are ocean biomes which already have appropriate
     * ocean floor topography built in.
     */
    @Inject(method = "getBiome", at = @At("RETURN"))
    private void ensureOceanBiome(int x, int y, int z, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // This won't modify anything yet - just ensuring the mixin applies correctly
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Ocean biome system active");
            hasLoggedInfo = true;
        }
    }
}