package me.shreyjain.smpCurrency.commands;

import me.shreyjain.smpCurrency.SmpCurrency;
import me.shreyjain.smpCurrency.managers.NPCManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command for managing currency exchange NPCs
 */
public class NPCCommand implements CommandExecutor, TabCompleter {
    private final SmpCurrency plugin;

    public NPCCommand(SmpCurrency plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("smpcurrency.npc.manage")) {
            player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        if (!plugin.getNPCManager().isCitizensEnabled()) {
            player.sendMessage(Component.text("Citizens plugin is not installed or enabled!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setexchange":
                return handleSetExchange(player);
            case "setbank":
                return handleSetBank(player);
            case "remove":
                return handleRemove(player);
            case "list":
                return handleList(player);
            default:
                sendHelp(player);
                return true;
        }
    }

    private boolean handleSetExchange(Player player) {
        NPC npc = getTargetNPC(player);
        if (npc == null) {
            player.sendMessage(Component.text("You must be looking at an NPC!", NamedTextColor.RED));
            return true;
        }

        plugin.getNPCManager().registerExchangeNPC(npc.getId());
        plugin.getNPCManager().save();
        player.sendMessage(Component.text("NPC #" + npc.getId() + " (" + npc.getName() + ") set as EXCHANGE NPC!", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleSetBank(Player player) {
        NPC npc = getTargetNPC(player);
        if (npc == null) {
            player.sendMessage(Component.text("You must be looking at an NPC!", NamedTextColor.RED));
            return true;
        }

        plugin.getNPCManager().registerBankNPC(npc.getId());
        plugin.getNPCManager().save();
        player.sendMessage(Component.text("NPC #" + npc.getId() + " (" + npc.getName() + ") set as BANK NPC!", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleRemove(Player player) {
        NPC npc = getTargetNPC(player);
        if (npc == null) {
            player.sendMessage(Component.text("You must be looking at an NPC!", NamedTextColor.RED));
            return true;
        }

        if (!plugin.getNPCManager().isRegisteredNPC(npc.getId())) {
            player.sendMessage(Component.text("This NPC is not registered!", NamedTextColor.RED));
            return true;
        }

        plugin.getNPCManager().unregisterNPC(npc.getId());
        plugin.getNPCManager().save();
        player.sendMessage(Component.text("NPC #" + npc.getId() + " (" + npc.getName() + ") unregistered!", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleList(Player player) {
        var npcs = plugin.getNPCManager().getAllNPCs();
        if (npcs.isEmpty()) {
            player.sendMessage(Component.text("No NPCs registered!", NamedTextColor.YELLOW));
            return true;
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
        return true;
    }

    private NPC getTargetNPC(Player player) {
        Entity target = player.getTargetEntity(5);
        if (target == null) {
            return null;
        }
        return CitizensAPI.getNPCRegistry().getNPC(target);
    }

    private void sendHelp(Player player) {
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("NPC Management Commands", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/npc setexchange - Set NPC as exchange NPC", NamedTextColor.WHITE));
        player.sendMessage(Component.text("/npc setbank - Set NPC as bank NPC", NamedTextColor.WHITE));
        player.sendMessage(Component.text("/npc remove - Remove NPC registration", NamedTextColor.WHITE));
        player.sendMessage(Component.text("/npc list - List all registered NPCs", NamedTextColor.WHITE));
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("setexchange", "setbank", "remove", "list");
        }
        return new ArrayList<>();
    }
}

