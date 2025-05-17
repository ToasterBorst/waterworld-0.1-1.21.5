package waterworld.mixin;

import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import waterworld.ProjectWaterworld;

@Mixin(ChunkNoiseSampler.class)
public class NoiseToHeightmapMixin {

    private static final int SEA_LEVEL = 63;
    
    /**
     * This targets the final stage where noise values are turned into actual terrain
     */
    @ModifyVariable(
        method = "sampleBlockState",
        at = @At("STORE"),
        ordinal = 0
    )
    private double modifyNoiseSample(double originalValue) {
        // If this noise value would create terrain above sea level, modify it
        // In Minecraft's noise system, positive is air, negative is solid terrain
        if (originalValue > -0.5) {
            // Make it very negative to ensure underwater terrain
            double newValue = -0.7;
            
            // Log occasionally
            if (Math.random() < 0.0001) {
                ProjectWaterworld.LOGGER.info("Noise: {} modified to {}", originalValue, newValue);
            }
            
            return newValue;
        }
        
        return originalValue;
    }
    
    /**
     * This modifies the erosion parameters which affect terrain height
     */
    @ModifyVariable(
        method = "<init>",
        at = @At("HEAD"),
        ordinal = 0
    )
    private static GenerationShapeConfig modifyGenerationShape(GenerationShapeConfig config) {
        ProjectWaterworld.LOGGER.info("Modifying generation shape config");
        
        // This is where we could modify terrain generation parameters
        // For now, just log that we're reaching this point
        
        return config;
    }
}