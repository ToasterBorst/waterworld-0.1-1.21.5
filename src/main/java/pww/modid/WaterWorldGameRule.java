package pww.modid;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class WaterWorldGameRule {
    
    public static void register() {
        // Register command to display info about waterworld
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                Commands.literal("waterworld")
                    .executes(context -> {
                        // Just show info for now
                        context.getSource().sendSuccess(
                            () -> Component.literal("Project Waterworld is active! Water level is set to " + WaterWorldConstants.WATER_LEVEL), 
                            false
                        );
                        return 1;
                    })
            );
        });
    }
}