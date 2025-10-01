package me.shreyjain.smpCurrency.commands;

import me.shreyjain.smpCurrency.SmpCurrency;
import me.shreyjain.smpCurrency.managers.CoinManager;
import me.shreyjain.smpCurrency.managers.ResourcePackManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CurrencyCommand implements TabExecutor {

    private final SmpCurrency plugin;

    public CurrencyCommand(SmpCurrency plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <give|list|texturepack|reload>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "give" -> handleGive(sender, label, args);
            case "list" -> handleList(sender);
            case "texturepack" -> handleTexturePack(sender);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /" + label + " list");
        }
        return true;
    }

    private void handleGive(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("smpcurrency.give")) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
            return;
        }
        // Require at least: give <player> <coinId>
        if (args.length < 3) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " give <player> <coinId> [amount]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found: {player}").replace("{player}", args[1])));
            return;
        }
        String coinId = args[2];
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(color(plugin.getConfig().getString("messages.invalid-amount", "&cInvalid amount: {amount}").replace("{amount}", args[3])));
                return;
            }
        }
        CoinManager coinManager = plugin.getCoinManager();
        ItemStack coin = coinManager.createCoinItem(coinId, amount);
        target.getInventory().addItem(coin);
        sender.sendMessage(color(plugin.getConfig().getString("messages.coin-given", "&aGave {amount} {coin} coin(s) to {player}")
            .replace("{amount}", String.valueOf(amount))
            .replace("{coin}", coinId)
            .replace("{player}", target.getName())));
        target.sendMessage(color(plugin.getConfig().getString("messages.coin-received", "&aYou received {amount} {coin} coin(s)!")
            .replace("{amount}", String.valueOf(amount))
            .replace("{coin}", coinId)));
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("smpcurrency.list")) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
            return;
        }
        CoinManager coinManager = plugin.getCoinManager();
        if (coinManager.getCoinIds().isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No coins are configured.");
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "Available coins:" + ChatColor.YELLOW + " " + String.join(", ", coinManager.getCoinIds()));
    }

    private void handleTexturePack(CommandSender sender) {
        ResourcePackManager rpm = plugin.getResourcePackManager();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }
        if (!rpm.isConfigured()) {
            sender.sendMessage(ChatColor.RED + "Resource pack not configured.");
            return;
        }
        rpm.applyResourcePack(player, true);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("smpcurrency.reload")) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "&cNo permission.")));
            return;
        }
        plugin.getCoinManager().reload();
        plugin.getResourcePackManager().reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Currency config and resource pack settings reloaded.");
    }

    private String color(String legacy) {
        return ChatColor.translateAlternateColorCodes('&', legacy);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            List<String> base = List.of("give", "list", "texturepack", "reload");
            for (String b : base) if (b.startsWith(args[0].toLowerCase(Locale.ROOT))) results.add(b);
            return results;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix)).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return plugin.getCoinManager().getCoinIds().stream().filter(id -> id.startsWith(prefix)).sorted().collect(Collectors.toList());
        }
        return results;
    }
}
