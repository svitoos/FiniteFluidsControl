package svitoos.mcmods.ffc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.tag.FluidTags.CachingTag;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.Logger;
import svitoos.mcmods.ffc.ModConfig.FluidSection;

public class FluidControlData {
  private static final Logger LOGGER = ModLogManager.getLogger();

  private static final List<FluidControlData> registry = new ArrayList<>();

  private static List<FluidSection> delayedConfig = new ArrayList<>();

  private final Fluid fluid;
  private final Tag<Fluid> tag;
  private final boolean allowInfinite;
  private final List<Category> infiniteBiomeCategories;
  private final List<Biome> infiniteBiomes;
  private final List<DimensionType> ininiteDimensions;
  private final int minHeight;
  private final int maxHeight;

  private FluidControlData(
      Fluid fluid,
      Tag<Fluid> tag,
      boolean allowInfinite,
      List<Category> infiniteBiomeCategories,
      List<Biome> infiniteBiomes,
      List<DimensionType> infiniteDimensions,
      int minHeight,
      int maxHeight) {
    this.fluid = fluid;
    this.tag = tag;
    this.allowInfinite = allowInfinite;
    this.infiniteBiomeCategories = infiniteBiomeCategories;
    this.infiniteBiomes = infiniteBiomes;
    this.ininiteDimensions = infiniteDimensions;
    this.minHeight = minHeight;
    this.maxHeight = maxHeight;
  }

  public boolean matches(Fluid fluid) {
    return fluid.matches(tag) || fluid.matchesType(this.fluid);
  }

  public boolean isInfinite(
      Biome.Category category, Biome biome, DimensionType dimensionType, BlockPos blockPos) {
    return allowInfinite
        && (infiniteBiomeCategories.isEmpty() || infiniteBiomeCategories.contains(category))
        && (infiniteBiomes.isEmpty() || infiniteBiomes.contains(biome))
        && (ininiteDimensions.isEmpty() || ininiteDimensions.contains(dimensionType))
        && (blockPos.getY() >= minHeight)
        && (blockPos.getY() <= maxHeight);
  }

  public static List<FluidControlData> getData() {
    if (delayedConfig != null) {
      delayedConfig.forEach(FluidControlData::delayedRegister);
      delayedConfig = null;
    }
    return registry;
  }

  private static void delayedRegister(FluidSection fluidConfig) {
    final List<Category> categories = new ArrayList<>();
    final List<Biome> biomes = new ArrayList<>();
    final List<DimensionType> dimensions = new ArrayList<>();

    if (fluidConfig.infiniteBiomesCategory != null) {
      final Map<String, Category> categoryNameMap =
          Arrays.stream(Category.values()).collect(Collectors.toMap(Category::getName, (c) -> c));
      fluidConfig.infiniteBiomesCategory.forEach(
          name -> {
            final Biome.Category category = categoryNameMap.get(name);
            if (category == null) {
              LOGGER.error("Invalid category '{}'", name);
            } else {
              categories.add(category);
            }
          });
    }

    if (fluidConfig.infiniteBiomes != null) {
      fluidConfig.infiniteBiomes.forEach(
          id -> {
            final Biome biome = Registry.BIOME.get(Identifier.tryParse(id));
            if (biome == null) {
              LOGGER.error("Invalid biome '{}'", id);
            } else {
              biomes.add(biome);
            }
          });
    }

    if (fluidConfig.infiniteDimensions != null) {
      fluidConfig.infiniteDimensions.forEach(
          id -> {
            final DimensionType dimension = Registry.DIMENSION.get(Identifier.tryParse(id));
            if (dimension == null) {
              LOGGER.error("Invalid dimension '{}'", id);
            } else {
              dimensions.add(dimension);
            }
          });
    }

    final Identifier fluidId = Identifier.tryParse(fluidConfig.id);
    final Fluid fluid = Registry.FLUID.get(fluidId);
    if (fluid.matchesType(Fluids.field_15906)) {
      LOGGER.error("Invalid fluid '{}'", fluidId);
      return;
    }

    int minHeight = fluidConfig.minHeight == null ? 0 : fluidConfig.minHeight;
    int maxHeight = fluidConfig.maxHeight == null ? 255 : fluidConfig.maxHeight;

    LOGGER.info(
        "Finite fluid '{}', except: biomeCategories = {}, biomes = {}, dimensions = {}, height = [{}, {}]",
        fluidId,
        categories,
        biomes,
        dimensions,
        minHeight,
        maxHeight);

    registry.add(
        new FluidControlData(
            fluid,
            new CachingTag(new Identifier(fluidConfig.id)),
            fluidConfig.allowInfinite,
            categories,
            biomes,
            dimensions,
            minHeight,
            maxHeight));
  }

  static void register(FluidSection fluidConfig) {

    if (fluidConfig.infiniteBiomes != null) {
      fluidConfig.infiniteBiomes.forEach(
          id -> {
            if (Identifier.tryParse(id) == null) {
              LOGGER.error("Invalid biome ID '{}'", id);
            }
          });
    }

    if (fluidConfig.infiniteDimensions != null) {
      fluidConfig.infiniteDimensions.forEach(
          id -> {
            if (Identifier.tryParse(id) == null) {
              LOGGER.error("Invalid dimension ID '{}'", id);
            }
          });
    }

    Identifier fluidId = Identifier.tryParse(fluidConfig.id);
    if (fluidId == null) {
      LOGGER.error("Invalid fluid ID '{}'", fluidConfig.id);
      return;
    }

    LOGGER.info("Added fluid '{}' to late initialization", fluidId);

    delayedConfig.add(fluidConfig);
  }
}
