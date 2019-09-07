package svitoos.mcmods.ffc;

import java.nio.file.Paths;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("unused")
public class FiniteFluidsControlMod implements ModInitializer, ClientModInitializer {

  static String modid = "finite-fluids-control";
  static String logPrefix = "FiniteFluidsControl";

  @Override
  public void onInitialize() {
    // This code runs as soon as Minecraft is in a mod-load-ready state.
    // However, some things (like resources) may still be uninitialized.
    // Proceed with mild caution.

    ModLogManager.init(modid, logPrefix);

    ModConfig.load(
            Paths.get(FabricLoader.getInstance().getConfigDirectory().getPath(), modid + ".toml"))
        .finiteFluids.stream()
        .filter(fluidSection -> fluidSection.enabled)
        .forEach(FluidControlData::register);
  }

  @Override
  public void onInitializeClient() {}
}
