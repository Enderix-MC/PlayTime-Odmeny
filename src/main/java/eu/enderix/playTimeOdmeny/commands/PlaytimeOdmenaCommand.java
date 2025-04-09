package eu.enderix.playTimeOdmeny.commands;

import eu.enderix.playTimeOdmeny.PlayTimeOdmeny;
import eu.enderix.playTimeOdmeny.Utils.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlaytimeOdmenaCommand implements CommandExecutor {

    private final File rewardFile;
    private final PlayTimeOdmeny plugin;
    private final ConfigUtil configUtil;
    private YamlConfiguration rewardConfig;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public PlaytimeOdmenaCommand(PlayTimeOdmeny playTimeOdmeny, PlayTimeOdmeny plugin, ConfigUtil configUtil) {
        this.plugin = plugin;
        this.configUtil = configUtil;
        rewardFile = new File(PlayTimeOdmeny.getInstance().getDataFolder(), "rewards.yml");
        if (!rewardFile.exists()) {
            try {
                rewardFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reloadConfig();
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Tento příkaz může použít jen hráč.");
                return true;
            }

            int cooldownSeconds = PlayTimeOdmeny.getInstance().getConfig().getInt("cooldown-seconds", 600);
            long currentTime = System.currentTimeMillis();
            UUID uuid = player.getUniqueId();

            if (cooldowns.containsKey(uuid) && currentTime - cooldowns.get(uuid) < cooldownSeconds * 1000) {
                long remaining = (cooldownSeconds * 1000 - (currentTime - cooldowns.get(uuid))) / 1000;
                String zbyva = Integer.toString((int) remaining);
                player.sendMessage(getMessage("cooldown").replace("%zbyva%", zbyva));
                return true;
            }

            String playtimeStr = PlaceholderAPI.setPlaceholders(player, "%cmi_user_playtime_hours%");
            if (playtimeStr == null || playtimeStr.isEmpty()) {
                player.sendMessage("Chyba při získávání playtime statistik.");
                return true;
            }

            long hours;
            try {
                hours = Long.parseLong(playtimeStr);
            } catch (NumberFormatException e) {
                player.sendMessage("Chyba při převodu playtime statistik na číslo.");
                return true;
            }

            boolean rewarded = false;

            // Projít odměny podle hodin
            for (String key : PlayTimeOdmeny.getInstance().getConfig().getConfigurationSection("rewards").getKeys(false)) {
                int rewardHour = Integer.parseInt(key);
                if (hours >= rewardHour && !hasReceivedReward(uuid, rewardHour)) {
                    String cmd = PlayTimeOdmeny.getInstance().getConfig().getString("rewards." + key + ".command");
                    cmd = cmd.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    markRewardGiven(uuid, rewardHour);

                    String hodiny = Integer.toString(rewardHour);
                    player.sendMessage(getMessage("reward-recived").replace("%hodiny%", hodiny));
                    rewarded = true;
                }
            }

            if (!rewarded) {
                player.sendMessage(getMessage("NoNew-Rewards"));
            }

            cooldowns.put(uuid, currentTime);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("playtime.reload")) {
                reloadConfig();
                sender.sendMessage(getMessage("reload-message"));
            } else {
                sender.sendMessage(getMessage("no-permissions"));
            }
            return true;
        }
        return false;
    }


    private void reloadConfig() {
        PlayTimeOdmeny.getInstance().reloadConfig();
        rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
        configUtil.reloadConfig();
    }

    private boolean hasReceivedReward(UUID uuid, int level) {
        return rewardConfig.getBoolean(uuid.toString() + ".hour" + level, false);
    }

    private void markRewardGiven(UUID uuid, int level) {
        rewardConfig.set(uuid.toString() + ".hour" + level, true);
        try {
            rewardConfig.save(rewardFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getMessage(String path) {
        if (!configUtil.getFile().exists()) {
            plugin.saveResource("messages.yml", false);
        }

        String message = configUtil.getConfig().getString("messages." + path);
        if (message == null) {
            return "§cZpráva nebyla nalezena!";
        }
        return message.replaceAll("&", "§");
    }
}