package svitoos.mcmods.ffc.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.BaseFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import svitoos.mcmods.ffc.FluidControlData;

@SuppressWarnings("unused")
@Mixin(BaseFluid.class)
public abstract class FluidsControlMixin extends Fluid {

  @Shadow()
  abstract boolean isInfinite();

  @Redirect(
      method =
          "getUpdatedState(Lnet/minecraft/world/ViewableWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/fluid/FluidState;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/BaseFluid;isInfinite()Z"))
  private boolean isInfiniteHook(
      @SuppressWarnings("unused") BaseFluid obj,
      ViewableWorld viewableWorld,
      BlockPos blockPos,
      BlockState blockState) {
    boolean allowInfinite = isInfinite();
    final Biome biome = viewableWorld.getBiome(blockPos);
    final Category category = biome.getCategory();
    final DimensionType dimension = viewableWorld.getDimension().getType();
    for (FluidControlData fluidControlData : FluidControlData.getData()) {
      if (fluidControlData.matches(this)) {
        allowInfinite = fluidControlData.isInfinite(category, biome, dimension, blockPos);
        if (allowInfinite) {
          break;
        }
      }
    }
    return allowInfinite;
  }
}
