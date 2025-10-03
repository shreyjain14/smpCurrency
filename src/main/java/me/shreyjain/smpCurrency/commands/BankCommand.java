package me.shreyjain.smpCurrency.commands;

import me.shreyjain.smpCurrency.SmpCurrency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command for managing the diamond bank
 */
public class BankCommand implements CommandExecutor, TabCompleter {
    private final SmpCurrency plugin;

    public BankCommand(SmpCurrency plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("smpcurrency.bank.manage")) {
            player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "balance":
            case "bal":
                return handleBalance(player);
            case "withdraw":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /bank withdraw <amount>", NamedTextColor.RED));
                    return true;
                }
                return handleWithdraw(player, args[1]);
            case "deposit":
                if (args.length < 2) {
                    player.sendMessage(Component.text("Usage: /bank deposit <amount>", NamedTextColor.RED));
                    return true;
                }
                return handleDeposit(player, args[1]);
            default:
                sendHelp(player);
                return true;
        }
    }

    private boolean handleBalance(Player player) {
        int totalDiamonds = plugin.getDiamondBankManager().getTotalDiamonds();
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Diamond Bank", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Total Diamonds: " + totalDiamonds, NamedTextColor.WHITE));
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        return true;
    }

    private boolean handleWithdraw(Player player, String amountStr) {
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                player.sendMessage(Component.text("Amount must be greater than 0!", NamedTextColor.RED));
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid amount: " + amountStr, NamedTextColor.RED));
            return true;
        }

        // Check if bank has enough diamonds
        if (!plugin.getDiamondBankManager().removeDiamonds(amount)) {
            player.sendMessage(Component.text("Not enough diamonds in the bank!", NamedTextColor.RED));
            player.sendMessage(Component.text("Available: " + plugin.getDiamondBankManager().getTotalDiamonds(), NamedTextColor.GRAY));
            return true;
        }

        // Give diamonds to player
        ItemStack diamonds = new ItemStack(Material.DIAMOND, amount);
        player.getInventory().addItem(diamonds);

        // Log withdrawal
        plugin.getTransactionLogger().logBankWithdrawal(player, amount);

        player.sendMessage(Component.text("Successfully withdrew " + amount + " diamond(s) from the bank!", NamedTextColor.GREEN));
        return true;
    }

    private boolean handleDeposit(Player player, String amountStr) {
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
            if (amount <= 0) {
                player.sendMessage(Component.text("Amount must be greater than 0!", NamedTextColor.RED));
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid amount: " + amountStr, NamedTextColor.RED));
            return true;
        }

        // Check if player has enough diamonds
        ItemStack[] contents = player.getInventory().getContents();
        int playerDiamonds = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() == Material.DIAMOND) {
                playerDiamonds += item.getAmount();
            }
        }

        if (playerDiamonds < amount) {
            player.sendMessage(Component.text("You don't have enough diamonds!", NamedTextColor.RED));
            player.sendMessage(Component.text("Your Diamonds: " + playerDiamonds, NamedTextColor.GRAY));
            return true;
        }

        // Remove diamonds from player
        player.getInventory().removeItem(new ItemStack(Material.DIAMOND, amount));

        // Add diamonds to bank
        plugin.getDiamondBankManager().addDiamonds(amount);

        // Log deposit
        plugin.getTransactionLogger().logBankDeposit(player, amount);

        player.sendMessage(Component.text("Successfully deposited " + amount + " diamond(s) to the bank!", NamedTextColor.GREEN));
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Bank Commands", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/bank balance - Check bank balance", NamedTextColor.WHITE));
        player.sendMessage(Component.text("/bank withdraw <amount> - Withdraw diamonds", NamedTextColor.WHITE));
        player.sendMessage(Component.text("/bank deposit <amount> - Deposit diamonds", NamedTextColor.WHITE));
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("balance", "withdraw", "deposit");
        }
        return new ArrayList<>();
    }
}
