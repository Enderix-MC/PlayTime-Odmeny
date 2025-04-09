package eu.enderix.playTimeOdmeny.commands;
import eu.enderix.playTimeOdmeny.PlayTimeOdmeny;
import eu.enderix.playTimeOdmeny.Utils.ConfigUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final PlayTimeOdmeny plugin;
    private final ConfigUtil configUtil;

    public ReloadCommand(PlayTimeOdmeny plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("pto.reload")) {
            reloadConfig();
            sender.sendMessage("§aConfig byl úspěšně reloadován.");
        } else {
            sender.sendMessage("§cNemáš oprávnění pro tento příkaz.");
        }
        return true;
    }

    private void reloadConfig() {
        PlayTimeOdmeny.getInstance().reloadConfig();
    }
}