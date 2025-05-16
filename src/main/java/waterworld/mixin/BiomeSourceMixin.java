package waterworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin {
    
    @ModifyVariable(method = "getBiome(IIILnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;)Lnet/minecraft/registry/entry/RegistryEntry;", 
                   at = @At("RETURN"), ordinal = 0)
    private RegistryEntry<Biome> onlyOceanBiomes(RegistryEntry<Biome> original) {
        // Check if this biome has a key
        if (original.getKey().isPresent()) {
            // Get the key
            RegistryKey<Biome> biomeKey = original.getKey().get();
            
            // Check if this biome is not an ocean biome
            if (!isOceanBiome(biomeKey)) {
                // Force the biome to be DEEP_OCEAN
                // This is a simplified approach - we could add more variety later
                return original.getRegistry().getEntry(BiomeKeys.DEEP_OCEAN).orElse(original);
            }
        }
        
        // Return the original biome if it's already an ocean biome
        return original;
    }
    
    private boolean isOceanBiome(RegistryKey<Biome> biomeKey) {
        return biomeKey == BiomeKeys.OCEAN || 
               biomeKey == BiomeKeys.DEEP_OCEAN ||
               biomeKey == BiomeKeys.FROZEN_OCEAN ||
               biomeKey == BiomeKeys.DEEP_FROZEN_OCEAN ||
               biomeKey == BiomeKeys.COLD_OCEAN ||
               biomeKey == BiomeKeys.DEEP_COLD_OCEAN ||
               biomeKey == BiomeKeys.LUKEWARM_OCEAN ||
               biomeKey == BiomeKeys.DEEP_LUKEWARM_OCEAN ||
               biomeKey == BiomeKeys.WARM_OCEAN;
    }
}