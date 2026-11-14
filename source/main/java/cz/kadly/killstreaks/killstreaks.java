package cz.kadly.killstreaks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class KillStreaks extends JavaPlugin implements Listener {

    private File dataFile;
    private YamlConfiguration data;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadPlayerData();

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("KillStreaks 1.0 has been enabled.");
    }

    @Override
    public void onDisable() {
        savePlayerData();
        getLogger().info("KillStreaks 1.0 has been disabled.");
    }

    private void loadPlayerData() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().severe("Could not create plugin data folder!");
        }

        dataFile = new File(getDataFolder(), "stats.yml");
        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile()) {
                    getLogger().warning("Could not create stats.yml.");
                }
            } catch (IOException exception) {
                getLogger().severe("Could not create stats.yml: " + exception.getMessage());
            }
        }

        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void savePlayerData() {
        if (data == null || dataFile == null) {
            return;
        }

        try {
            data.save(dataFile);
        } catch (IOException exception) {
            getLogger().severe("Could not save stats.yml: " + exception.getMessage());
        }
    }

    private int getStreak(UUID uuid) {
        return data.getInt("players." + uuid + ".streak", 0);
    }

    private void setStreak(UUID uuid, int streak) {
        data.set("players." + uuid + ".streak", Math.max(0, streak));
        savePlayerData();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Reset only when the player is killed by another player.
        if (killer == null || killer.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        setStreak(victim.getUniqueId(), 0);

        int newStreak = getStreak(killer.getUniqueId()) + 1;
        setStreak(killer.getUniqueId(), newStreak);

        String killMessage = getMessage("player-kill")
                .replace("%killer%", killer.getName())
                .replace("%victim%", victim.getName())
                .replace("%streak%", String.valueOf(newStreak));

        Bukkit.broadcastMessage(color(killMessage));

        if (getConfig().getIntegerList("milestones").contains(newStreak)) {
            String milestoneMessage = getMessage("milestone")
                    .replace("%player%", killer.getName())
                    .replace("%streak%", String.valueOf(newStreak));

            Bukkit.broadcastMessage(color(milestoneMessage));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(color(getMessage("player-not-found")));
                return true;
            }

            sendOwnStreak(sender, player.getUniqueId());
            return true;
        }

        if (!sender.hasPermission("killstreaks.admin")) {
            sender.sendMessage(color(getMessage("no-permission")));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target != null) {
            sendOtherStreak(sender, target.getName(), target.getUniqueId());
            return true;
        }

        // Also allow checking players who are currently offline, by exact name.
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayerIfCached(args[0]);
        if (offlineTarget == null || offlineTarget.getName() == null) {
            sender.sendMessage(color(getMessage("player-not-found")));
            return true;
        }

        sendOtherStreak(sender, offlineTarget.getName(), offlineTarget.getUniqueId());
        return true;
    }

    private void sendOwnStreak(CommandSender sender, UUID uuid) {
        String message = getMessage("own-streak")
                .replace("%streak%", String.valueOf(getStreak(uuid)));
        sender.sendMessage(color(message));
    }

    private void sendOtherStreak(CommandSender sender, String playerName, UUID uuid) {
        String message = getMessage("other-streak")
                .replace("%player%", playerName)
                .replace("%streak%", String.valueOf(getStreak(uuid)));
        sender.sendMessage(color(message));
    }

    private String getMessage(String path) {
        return getConfig().getString("messages." + path, "&cMissing message: " + path);
    }

    private String color(String message) {
        String prefix = getConfig().getString("messages.prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
}
