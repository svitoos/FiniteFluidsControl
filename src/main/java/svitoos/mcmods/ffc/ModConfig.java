package svitoos.mcmods.ffc;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.Logger;

class ModConfig {
  private static final Logger LOGGER = ModLogManager.getLogger();

  List<FluidSection> finiteFluids =
      Arrays.asList(
          new FluidSection(
              true,
              "water",
              true,
              Collections.emptyList(),
              Arrays.asList("river", "ocean", "swamp", "beach"),
              Collections.emptyList(),
              null,
              62),
          new FluidSection(
              true,
              "lava",
              true,
              Collections.emptyList(),
              Collections.emptyList(),
              Collections.singletonList("the_nether"),
              null,
              31));

  static class FluidSection {

    boolean enabled;
    String id;
    boolean allowInfinite;
    List<String> infiniteBiomes;
    List<String> infiniteBiomesCategory;
    List<String> infiniteDimensions;
    Integer minHeight;
    Integer maxHeight;

    FluidSection(
        boolean enabled,
        String id,
        boolean allowInfinite,
        List<String> infiniteBiomes,
        List<String> infiniteBiomesCategory,
        List<String> infiniteDimensions,
        Integer minHeight,
        Integer maxHeight) {
      this.enabled = enabled;
      this.id = id;
      this.allowInfinite = allowInfinite;
      this.infiniteBiomes = infiniteBiomes;
      this.infiniteBiomesCategory = infiniteBiomesCategory;
      this.infiniteDimensions = infiniteDimensions;
      this.minHeight = minHeight;
      this.maxHeight = maxHeight;
    }
  }

  static ModConfig load(Path path) {
    ModConfig config = new ModConfig();
    if (Files.exists(path)) {
      try {
        Toml configToml = new Toml().read(Files.newInputStream(path));
        config = configToml.to(ModConfig.class);
      } catch (IOException e) {
        LOGGER.error("Failed to load config! Use default config.");
        e.printStackTrace();
        return config;
      }
    }
    save(path, config);
    return config;
  }

  private static void save(Path path, ModConfig config) {
    try {
      OutputStream f = Files.newOutputStream(path);
      f.write(
          ("# List of fluids to be finite.\n"
                  + "#\n"
                  + "# Fluid section fields:\n"
                  + "#   - enabled - If false then the section is ignored. [Required; Type: boolean]\n"
                  + "#\n"
                  + "#   - id - Fluid ID. Eg \"minecraft:water\", \"lava\". [Required; Type: string]\n"
                  + "#\n"
                  + "#   - allowInfinite - If true and all other 'infinite' conditions in this section are true then the fluid will be infinite. [Optional; Type: boolean]\n"
                  + "#\n"
                  + "#   - infiniteBiomes - The list of biome ID's in which the fluid will be infinite. [Optional; Type: string list; Empty list ignored]\n"
                  + "#\n"
                  + "#   - infiniteBiomesCategory - The list of biome categories in which the fluid will be infinite.\n"
                  + "#     Possible values: taiga, extreme_hills, jungle, mesa, plains, savanna, icy, the_end,\n"
                  + "#     beach, forest, ocean, desert, river, swamp, mushroom, nether. [Optional; Type: string list; Empty list ignored]\n"
                  + "#\n"
                  + "#   - infiniteDimensions - The list of dimension ID's in which the fluid will be infinite. Possible\n"
                  + "#     values for vanilla: overworld, the_nether, the_end. [Optional; Type: string list; Empty list ignored.]\n"
                  + "#\n"
                  + "#   - minHeight/maxHeight - Minimum/Maximum y-level in which the fluid be infinite. [Optional, Type: integer]\n"
                  + "#\n"
                  + "# The fluid will be infinite if at least in one of the sections all 'infinite' conditions are true.\n"
                  + "\n")
              .getBytes());
      new TomlWriter().write(config, f);
    } catch (IOException e) {
      LOGGER.error("Failed to save config!");
      e.printStackTrace();
    }
  }
}
