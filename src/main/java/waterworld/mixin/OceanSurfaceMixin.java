package waterworld.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(ChunkGenerator.class)
public class OceanSurfaceMixin {

    private static boolean hasLoggedInfo = false;

    /**
     * This mixin intercepts chunk generation to replace any blocks above sea level with water.
     * This ensures we have proper ocean floors without land sticking out.
     */
    @Inject(method = "generateFeatures", at = @At("RETURN"))
    private void ensureWaterAboveSeaLevel(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor, CallbackInfoReturnable<Chunk> cir) {
        // Only apply in the overworld
        if (!world.getDimension().natural()) {
            return;
        }
        
        // Log once
        if (!hasLoggedInfo) {
            ProjectWaterworld.LOGGER.info("Waterworld: Ensuring ocean covers all land");
            hasLoggedInfo = true;
        }
        
        // Normal Minecraft sea level
        int seaLevel = 63;
        
        // Get the chunk's coordinates
        int chunkX = chunk.getPos().getStartX();
        int chunkZ = chunk.getPos().getStartZ();
        
        // Replace all blocks above sea level with water
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = chunk.getHeightmap(net.minecraft.world.Heightmap.Type.WORLD_SURFACE).get(x, z);
                
                // If the height is above sea level, we need to replace with water
                for (int y = seaLevel + 1; y <= height + 1; y++) {
                    BlockPos pos = new BlockPos(chunkX + x, y, chunkZ + z);
                    
                    // Use the world to set block states, not the chunk directly
                    BlockState currentState = world.getBlockState(pos);
                    
                    // Don't replace air or existing water/waterlogged blocks
                    if (!currentState.isAir() && 
                        currentState.getBlock() != Blocks.WATER && 
                        !currentState.getFluidState().isOf(net.minecraft.fluid.Fluids.WATER)) {
                        
                        // Use the StructureWorldAccess to set blocks properly
                        world.setBlockState(pos, Blocks.WATER.getDefaultState(), 2);
                    }
                }
            }
        }
    }
}