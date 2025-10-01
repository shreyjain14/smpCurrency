package me.shreyjain.smpCurrency.listeners;

import me.shreyjain.smpCurrency.SmpCurrency;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final SmpCurrency plugin;

    public PlayerJoinListener(SmpCurrency plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Wait a bit before showing the prompt to ensure the player is fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline() && plugin.getResourcePackManager().isConfigured()) {
                    plugin.getResourcePackManager().promptPlayerForResourcePack(event.getPlayer());
                }
            }
        }.runTaskLater(plugin, 60L); // 3 seconds delay
    }
}
