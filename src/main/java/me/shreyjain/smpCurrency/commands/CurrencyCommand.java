package me.shreyjain.smpCurrency.commands;

import me.shreyjain.smpCurrency.SmpCurrency;
import me.shreyjain.smpCurrency.managers.CoinManager;
import me.shreyjain.smpCurrency.managers.ResourcePackManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
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
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <give|list|texturepack|reload|npc>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "give" -> handleGive(sender, label, args);
            case "list" -> handleList(sender);
            case "texturepack" -> handleTexturePack(sender);
            case "reload" -> handleReload(sender);
            case "npc" -> handleNPC(sender, args);
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /" + label + " for help");
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

    private void handleNPC(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return;
        }

        if (!player.hasPermission("smpcurrency.npc.manage")) {
            player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return;
        }

        if (!plugin.getNPCManager().isCitizensEnabled()) {
            player.sendMessage(Component.text("Citizens plugin is not installed or enabled!", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sendNPCHelp(player);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "setexchange" -> handleSetExchange(player);
            case "setbank" -> handleSetBank(player);
            case "remove" -> handleRemoveNPC(player);
            case "list" -> handleListNPCs(player);
            default -> sendNPCHelp(player);
        }
    }

    private void handleSetExchange(Player player) {
        NPC npc = getTargetNPC(player);
        if (npc == null) {
            player.sendMessage(Component.text("You must be looking at an NPC!", NamedTextColor.RED));
            return;
        }

        plugin.getNPCManager().registerExchangeNPC(npc.getId());
        plugin.getNPCManager().save();
        player.sendMessage(Component.text("NPC #" + npc.getId() + " (" + npc.getName() + ") set as EXCHANGE NPC!", NamedTextColor.GREEN));
    }

    private void handleSetBank(Player player) {
        NPC npc = getTargetNPC(player);
        if (npc == null) {
            player.sendMessage(Component.text("You must be looking at an NPC!", NamedTextColor.RED));
            return;
        }

        plugin.getNPCManager().registerBankNPC(npc.getId());
        plugin.getNPCManager().save();
        player.sendMessage(Component.text("NPC #" + npc.getId() + " (" + npc.getName() + ") set as BANK NPC!", NamedTextColor.GREEN));
    }

    private void handleRemoveNPC(Player player) {
        NPC npc = getTargetNPC(player);
        if (npc == null) {
            player.sendMessage(Component.text("You must be looking at an NPC!", NamedTextColor.RED));
            return;
        }

        if (!plugin.getNPCManager().isRegisteredNPC(npc.getId())) {
            player.sendMessage(Component.text("This NPC is not registered!", NamedTextColor.RED));
            return;
        }

        plugin.getNPCManager().unregisterNPC(npc.getId());
        plugin.getNPCManager().save();
        player.sendMessage(Component.text("NPC #" + npc.getId() + " (" + npc.getName() + ") unregistered!", NamedTextColor.GREEN));
    }

    private void handleListNPCs(Player player) {
        var npcs = plugin.getNPCManager().getAllNPCs();
        if (npcs.isEmpty()) {
            player.sendMessage(Component.text("No NPCs registered!", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Registered NPCs", NamedTextColor.YELLOW));

        for (var entry : npcs.entrySet()) {
            NPC npc = CitizensAPI.getNPCRegistry().getById(entry.getKey());
            String npcName = npc != null ? npc.getName() : "Unknown";
            String type = entry.getValue().name();
            player.sendMessage(Component.text("  #" + entry.getKey() + " (" + npcName + ") - " + type, NamedTextColor.WHITE));
        }

        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
    }

    private NPC getTargetNPC(Player player) {
        Entity target = player.getTargetEntity(5);
        if (target == null) {
            return null;
        }
        return CitizensAPI.getNPCRegistry().getNPC(target);
    }

    private void sendNPCHelp(Player player) {
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("NPC Management Commands", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/currency npc setexchange - Set NPC for diamond/coin exchange", NamedTextColor.WHITE));
        player.sendMessage(Component.text("  (Converts diamonds→coins or coins→diamonds)", NamedTextColor.GRAY));
        player.sendMessage(Component.text("  Right-click = 1 item, Shift+Right-click = full stack", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/currency npc setbank - Set NPC as bank NPC", NamedTextColor.WHITE));
        player.sendMessage(Component.text("/currency npc remove - Remove NPC registration", NamedTextColor.WHITE));
        player.sendMessage(Component.text("/currency npc list - List all registered NPCs", NamedTextColor.WHITE));
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
    }

    private String color(String legacy) {
        return ChatColor.translateAlternateColorCodes('&', legacy);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1) {
            List<String> base = List.of("give", "list", "texturepack", "reload", "npc");
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
        if (args.length == 2 && args[0].equalsIgnoreCase("npc")) {
            List<String> npcSubs = List.of("setexchange", "setbank", "remove", "list");
            for (String sub : npcSubs) if (sub.startsWith(args[1].toLowerCase(Locale.ROOT))) results.add(sub);
            return results;
        }
        return results;
    }
}
