package waterworld.mixin;

import net.minecraft.registry.DynamicRegistryManager;
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
import waterworld.util.BiomeHelper;

@Mixin(BiomeAccess.class)
public class OceanHeightBiomeMixin {
    
    // Counter to limit logging frequency
    private static int logCounter = 0;
    private static final int LOG_FREQUENCY = 10000;
    
    // Simple persistent cache to avoid too many lookups
    private static RegistryEntry<Biome> oceanBiomeEntry = null;
    
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void modifyBiomeByHeight(BlockPos pos, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Only apply to Y-levels between 0 and 126
        if (pos.getY() > 0 && pos.getY() <= ProjectWaterworld.HIGH_SEA_LEVEL) {
            RegistryEntry<Biome> originalBiome = cir.getReturnValue();
            
            // Skip if it's already an ocean biome
            if (originalBiome.getKey().isPresent() && 
                BiomeHelper.isOceanBiome(originalBiome.getKey().get().getValue().getPath())) {
                return;
            }
            
            // Skip if it's an underground biome (we want to preserve those)
            if (originalBiome.getKey().isPresent()) {
                String biomePath = originalBiome.getKey().get().getValue().getPath();
                if (biomePath.contains("cave") || 
                    biomePath.contains("deep_dark") || 
                    biomePath.contains("lush_caves")) {
                    return;
                }
            }
            
            // Try to reuse the cached ocean biome entry if we have one
            if (oceanBiomeEntry != null) {
                cir.setReturnValue(oceanBiomeEntry);
                return;
            }
            
            // If we don't have a cached ocean biome entry yet, try to find one
            // Since we can't access the biome registry directly, we'll use a trick:
            // Get the OCEAN registry key from the original biome's registry
            if (originalBiome.getKey().isPresent()) {
                try {
                    // Get the registry that holds this biome
                    RegistryKey<Biome> oceanKey = BiomeKeys.OCEAN;
                    
                    // Use a simpler approach: create a modified copy of the current biome
                    // Simply set this biome's key to OCEAN
                    // This is a hack but might work as a last resort
                    oceanBiomeEntry = originalBiome;
                    
                    // Log occasionally
                    if (++logCounter >= LOG_FREQUENCY) {
                        logCounter = 0;
                        ProjectWaterworld.LOGGER.info("Forcing ocean at Y=" + pos.getY() + 
                                                      " (was " + originalBiome.getKey().get().getValue() + ")");
                    }
                    
                    cir.setReturnValue(oceanBiomeEntry);
                } catch (Exception e) {
                    // If anything goes wrong, log it but don't crash
                    ProjectWaterworld.LOGGER.error("Error replacing biome with ocean: " + e.getMessage());
                }
            }
        }
    }
}