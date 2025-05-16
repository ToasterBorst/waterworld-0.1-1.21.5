package waterworld.mixin;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import waterworld.ProjectWaterworld;

@Mixin(MultiNoiseUtil.class)
public class BiomeParameterMixin {
    
    private static boolean hasLoggedInfo = false;

    /**
     * This mixin targets the sample method in MultiNoiseUtil which is used for biome placement.
     * By modifying the continentalness parameter to always be in the ocean range (-1.2 to -0.8),
     * we ensure ocean biomes and their associated terrain are generated across the entire world.
     */
    @ModifyVariable(
        method = "sample(DDDDDDJ)Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$NoiseValuePoint;",
        at = @At("HEAD"),
        ordinal = 2  // continentalness is the third parameter (0-indexed)
    )
    private static double forceOceanContinentalness(double original) {
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Forcing ocean continentalness parameters");
            hasLoggedInfo = true;
        }
        
        // Force continentalness to be in the ocean range (-1.2 to -0.8)
        // This value determines ocean vs land, so we ensure it's always ocean
        return -1.0;
    }
}