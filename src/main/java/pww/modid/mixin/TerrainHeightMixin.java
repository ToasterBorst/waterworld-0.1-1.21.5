package pww.modid.mixin;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseRouter.class)
public class TerrainHeightMixin {
    private static final int MAX_TERRAIN_HEIGHT = 111; // 15 blocks below sea level (126)
    
    // Target the method that creates the terrain noise
    @Inject(method = "finalDensity()Lnet/minecraft/world/level/levelgen/DensityFunction;", at = @At("RETURN"), cancellable = true)
    private void limitTerrainHeight(CallbackInfoReturnable<DensityFunction> cir) {
        DensityFunction original = cir.getReturnValue();
        if (original != null) {
            // Create a new function that caps terrain height
            DensityFunction capped = DensityFunctions.min(
                original,
                // In Minecraft's noise system, negative values = air, positive = solid
                // So we need to ensure values are negative above our max height
                DensityFunctions.yClampedGradient(
                    MAX_TERRAIN_HEIGHT, // Start Y (inclusive)
                    MAX_TERRAIN_HEIGHT + 1, // End Y (exclusive)
                    1.0, // Value at start Y (solid)
                    -1.0 // Value at end Y (air)
                )
            );
            
            cir.setReturnValue(capped);
            System.out.println("[Waterworld] Capped terrain height at " + MAX_TERRAIN_HEIGHT);
        }
    }
}