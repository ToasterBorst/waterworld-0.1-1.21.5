package waterworld.mixin;

import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.Identifier;
import waterworld.ProjectWaterworld;

@Mixin(ChunkGenerator.class)
public class OceanTerrainMixin {

    // Set of ocean biome identifiers
    private static final Identifier[] OCEAN_BIOMES = {
        Identifier.of("minecraft", "ocean"),
        Identifier.of("minecraft", "deep_ocean"),
        Identifier.of("minecraft", "frozen_ocean"),
        Identifier.of("minecraft", "deep_frozen_ocean"),
        Identifier.of("minecraft", "cold_ocean"),
        Identifier.of("minecraft", "deep_cold_ocean"),
        Identifier.of("minecraft", "lukewarm_ocean"),
        Identifier.of("minecraft", "deep_lukewarm_ocean"),
        Identifier.of("minecraft", "warm_ocean")
    };

    @Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
    private void modifyOceanHeight(int x, int z, net.minecraft.world.Heightmap.Type heightmap, NoiseConfig noiseConfig, CallbackInfoReturnable<Integer> cir) {
        ChunkGenerator thisGenerator = (ChunkGenerator)(Object)this;
        BiomeSource biomeSource = thisGenerator.getBiomeSource();
        
        // Get the biome at this position
        RegistryKey<Biome> biomeKey = biomeSource.getBiome(x, 0, z, noiseConfig.getMultiNoiseSampler()).getKey().orElse(null);
        
        if (biomeKey != null) {
            Identifier biomeId = biomeKey.getValue();
            
            // Check if this is an ocean biome
            boolean isOceanBiome = false;
            for (Identifier oceanId : OCEAN_BIOMES) {
                if (oceanId.equals(biomeId)) {
                    isOceanBiome = true;
                    break;
                }
            }
            
            // If this is an ocean biome, make sure the height is below sea level
            if (isOceanBiome) {
                int currentHeight = cir.getReturnValueI();
                if (currentHeight > ProjectWaterworld.HIGH_SEA_LEVEL - 1) {
                    cir.setReturnValue(ProjectWaterworld.HIGH_SEA_LEVEL - 1);
                }
            }
        }
    }
}