package eu.enderix.playTimeOdmeny;

import eu.enderix.playTimeOdmeny.Utils.ConfigUtil;
import eu.enderix.playTimeOdmeny.commands.PlaytimeOdmenaCommand;
import eu.enderix.playTimeOdmeny.commands.ReloadCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PlayTimeOdmeny extends JavaPlugin {

    private static PlayTimeOdmeny instance;

    @Override
    public void onEnable() {
        PlayTimeOdmeny plugin = this;
        saveDefaultConfig();


        File configFile = new File(getDataFolder(), "messages.yml");
        if (!configFile.exists()) {
            saveResource("messages.yml", false);
        }

        ConfigUtil messages = new ConfigUtil(this, "messages.yml");

        instance = this;

        saveDefaultConfig();

        getCommand("playtime-odmena").setExecutor(new PlaytimeOdmenaCommand(plugin, plugin, messages));
        getCommand("pto-reload").setExecutor(new ReloadCommand(plugin, messages));
    }

    public static PlayTimeOdmeny getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
    }
}
