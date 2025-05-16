package waterworld.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import waterworld.ProjectWaterworld;

@Mixin(MinecraftServer.class)
public class SpawnPlatformMixin {

    private static boolean hasGeneratedSpawnPlatform = false;
    
    @Inject(method = "setupSpawn", at = @At("HEAD"))
    private static void generateSpawnPlatform(ServerWorld world, ServerWorldProperties worldProperties, 
                                          boolean bonusChest, boolean debugWorld, CallbackInfo ci) {
        // Only do this once and only in the overworld
        if (!hasGeneratedSpawnPlatform && world.getRegistryKey() == World.OVERWORLD) {
            ProjectWaterworld.LOGGER.info("Waterworld: Generating spawn platform");
            
            // Create a small platform at spawn
            BlockPos spawnPos = world.getSpawnPos();
            
            // Make a platform just above sea level
            int platformY = ProjectWaterworld.HIGH_SEA_LEVEL + 1;
            
            // Use stone as the platform material (or any other solid block)
            BlockState platformMaterial = Blocks.STONE.getDefaultState();
            
            // Create a small platform (7x7)
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos platformPos = new BlockPos(spawnPos.getX() + x, platformY, spawnPos.getZ() + z);
                    world.setBlockState(platformPos, platformMaterial, 0);
                }
            }
            
            // Update spawn position to be on the platform
            BlockPos newSpawnPos = new BlockPos(spawnPos.getX(), platformY + 1, spawnPos.getZ());
            world.setSpawnPos(newSpawnPos, 0.0f);
            
            hasGeneratedSpawnPlatform = true;
        }
    }
}