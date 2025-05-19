package waterworld.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.BiomeReplacementRegistry;

@Mixin(BiomeAccess.class)
public class BiomeAccessMixin {
    
    @Inject(method = "getBiomeForNoiseGen(III)Lnet/minecraft/registry/entry/RegistryEntry;", at = @At("RETURN"), cancellable = true)
    private void replaceBiomesAboveSeaLevel(int biomeX, int biomeY, int biomeZ, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        int worldY = biomeY * 4;
        if (worldY >= 127) {
            RegistryEntry<Biome> currentBiome = cir.getReturnValue();
            if (currentBiome != null && currentBiome.getKey().isPresent()) {
                RegistryEntry<Biome> replacementBiome = BiomeReplacementRegistry.getReplacementBiome(currentBiome);
                if (replacementBiome != null && replacementBiome != currentBiome && 
                    replacementBiome.getKey().isPresent() && replacementBiome.value() != null) {
                    cir.setReturnValue(replacementBiome);
                }
            }
        }
    }
}