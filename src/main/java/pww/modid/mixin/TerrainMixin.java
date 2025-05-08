package pww.modid.mixin;

import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

@Mixin(NoiseBasedChunkGenerator.class)
public class TerrainMixin {
    private static final int MAX_TERRAIN_HEIGHT = 111; // 15 blocks below sea level (126)
    
    // Use the exact method signature
    @Inject(method = "getHeightOnGround(IILnet/minecraft/world/level/levelgen/Heightmap$Types;)I", at = @At("RETURN"), cancellable = true)
    private void capGroundHeight(int x, int z, Heightmap.Types types, CallbackInfoReturnable<Integer> cir) {
        int height = cir.getReturnValue();
        // Cap the height to be at most 111
        if (height > MAX_TERRAIN_HEIGHT) {
            cir.setReturnValue(MAX_TERRAIN_HEIGHT);
            System.out.println("[Waterworld] Capped terrain height at " + MAX_TERRAIN_HEIGHT);
        }
    }
}