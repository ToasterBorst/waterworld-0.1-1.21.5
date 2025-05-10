package waterworld.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;
import waterworld.world.OceanBiomeSource;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    
    @Shadow @Final protected BiomeSource biomeSource;
    
    @Inject(method = "getBiomeForNoiseGen", at = @At("RETURN"), cancellable = true)
    private void onGetBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Convert biome coordinates to block coordinates
        int blockY = biomeY * 4; // Assuming a scale factor of 4 for Y
        
        // Apply our ocean biome rules when Y <= 126 and Y > 0
        if (OceanBiomeSource.shouldBeOcean(blockY)) {
            RegistryEntry<Biome> originalBiome = cir.getReturnValue();
            
            // Skip if already an ocean biome
            if (OceanBiomeSource.isOceanBiome(originalBiome)) {
                return;
            }
            
            // Skip if it's an underground biome (to preserve those)
            if (OceanBiomeSource.isUndergroundBiome(originalBiome)) {
                return;
            }
            
            // Log that we're detecting non-ocean biomes at this height
            if (Math.random() < 0.001) {
                ProjectWaterworld.LOGGER.info("ChunkGenerator found non-ocean biome at blockY=" + blockY + 
                    " (biomeY=" + biomeY + "): " + 
                    originalBiome.getKey().map(k -> k.getValue().toString()).orElse("unknown"));
            }
            
            // TODO: Replace with ocean biome once we figure out how to get the registry entry
        }
    }
}