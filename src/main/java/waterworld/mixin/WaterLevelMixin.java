package waterworld.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import waterworld.ProjectWaterworld;

@Mixin(WorldView.class)
public interface WaterLevelMixin {
    
    @Inject(method = "isWater", at = @At("HEAD"), cancellable = true)
    default void forceWaterEverywhere(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        WorldView worldView = (WorldView) (Object) this;
        
        // FIXED: Properly check if it's the overworld without using getRegistryKey()
        if (worldView instanceof WorldAccess) {
            WorldAccess worldAccess = (WorldAccess) worldView;
            
            // Check if it's the overworld using dimension properties instead of registry key
            if (worldView.getDimension().natural()) {
                // If it's above sea level, it's always water
                if (pos.getY() >= 63 && pos.getY() <= 180) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}