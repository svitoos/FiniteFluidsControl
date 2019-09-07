package svitoos.mcmods.ffc;

import net.fabricmc.loader.launch.common.FabricLauncherBase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.ObjectArrayMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;

abstract class ModLogManager {
  private static Logger logger;

  static Logger getLogger() {
    return logger;
  }

  static void init(String modid, String name) {
    logger = LogManager.getLogger(modid, new PrefixMessageFactory(name));
    if (FabricLauncherBase.getLauncher().isDevelopment()) {
      Configurator.setLevel(modid, Level.DEBUG);
    }
  }

  private static class PrefixMessageFactory implements MessageFactory {
    private String prefix;

    PrefixMessageFactory(String prefix) {
      this.prefix = "["+prefix+"]:";
    }

    @Override
    public Message newMessage(Object message) {
      return new ObjectArrayMessage(prefix, message);
    }

    @Override
    public Message newMessage(String message) {
      return new SimpleMessage(String.format("%s %s", prefix, message));
    }

    @Override
    public Message newMessage(String message, Object... params) {
      return new ParameterizedMessage(String.format("%s %s", prefix, message), params);
    }
  }
}
