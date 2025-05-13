package waterworld.util;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import waterworld.ProjectWaterworld;

public class TerrainDebugger {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("waterworld-debug")
                .executes(context -> {
                    BlockPos pos = context.getSource().getPlayer().getBlockPos();
                    int seaLevel = context.getSource().getWorld().getSeaLevel();
                    
                    context.getSource().sendMessage(Text.literal("Current position: " + pos));
                    context.getSource().sendMessage(Text.literal("Sea level: " + seaLevel));
                    context.getSource().sendMessage(Text.literal("Height above sea level: " + (pos.getY() - seaLevel)));
                    context.getSource().sendMessage(Text.literal("Target ocean floor max: " + ProjectWaterworld.VANILLA_OCEAN_FLOOR_MAX));
                    
                    return 1;
                }));
        });
    }
}