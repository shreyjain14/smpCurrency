package me.shreyjain.smpCurrency.managers;

import me.shreyjain.smpCurrency.SmpCurrency;
import me.shreyjain.smpCurrency.items.CustomItems;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages NPCs for currency exchange and diamond bank
 */
public class NPCManager {
    private final SmpCurrency plugin;
    private final Map<Integer, NPCType> npcTypes = new HashMap<>();

    public enum NPCType {
        EXCHANGE,      // Exchanges diamonds for coins OR coins for diamonds (both directions)
        BANK           // Stores diamonds for managers
    }

    public NPCManager(SmpCurrency plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if Citizens is available
     */
    public boolean isCitizensEnabled() {
        return Bukkit.getPluginManager().getPlugin("Citizens") != null;
    }

    /**
     * Register an NPC as an exchange NPC
     */
    public void registerExchangeNPC(int npcId) {
        npcTypes.put(npcId, NPCType.EXCHANGE);
        plugin.getLogger().info("Registered NPC #" + npcId + " as EXCHANGE NPC");
    }

    /**
     * Register an NPC as a bank NPC
     */
    public void registerBankNPC(int npcId) {
        npcTypes.put(npcId, NPCType.BANK);
        plugin.getLogger().info("Registered NPC #" + npcId + " as BANK NPC");
    }

    /**
     * Unregister an NPC
     */
    public void unregisterNPC(int npcId) {
        npcTypes.remove(npcId);
        plugin.getLogger().info("Unregistered NPC #" + npcId);
    }

    /**
     * Get the type of an NPC
     */
    public NPCType getNPCType(int npcId) {
        return npcTypes.get(npcId);
    }

    /**
     * Check if an NPC is registered
     */
    public boolean isRegisteredNPC(int npcId) {
        return npcTypes.containsKey(npcId);
    }

    /**
     * Handle exchange NPC interaction - supports both diamond->coin and coin->diamond
     * @param player The player interacting
     * @param isShiftClick Whether the player shift-clicked (converts entire stack)
     */
    public void handleExchangeNPC(Player player, boolean isShiftClick) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if holding diamonds
        if (itemInHand.getType() == Material.DIAMOND && itemInHand.getAmount() >= 1) {
            handleDiamondToCoin(player, itemInHand, isShiftClick);
        }
        // Check if holding coins
        else if (isSameCoin(itemInHand, CustomItems.createCoin(1))) {
            handleCoinToDiamond(player, itemInHand, isShiftClick);
        }
        // Not holding either
        else {
            player.sendMessage(Component.text("You must be holding diamonds or coins in your hand to exchange!", NamedTextColor.RED));
        }
    }

    /**
     * Convert diamonds to coins
     */
    private void handleDiamondToCoin(Player player, ItemStack itemInHand, boolean isShiftClick) {
        int amount = isShiftClick ? itemInHand.getAmount() : 1;

        // Remove diamonds from hand
        itemInHand.setAmount(itemInHand.getAmount() - amount);

        // Give coins to player
        ItemStack coins = CustomItems.createCoin(amount);
        player.getInventory().addItem(coins);

        // Add diamonds to bank
        plugin.getDiamondBankManager().addDiamonds(amount);

        // Log transaction
        plugin.getTransactionLogger().logTransaction(player, amount, amount);

        // Send message to player
        if (amount == 1) {
            player.sendMessage(Component.text("Successfully exchanged 1 diamond for 1 coin!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Successfully exchanged " + amount + " diamonds for " + amount + " coins!", NamedTextColor.GREEN));
        }
    }

    /**
     * Convert coins to diamonds
     */
    private void handleCoinToDiamond(Player player, ItemStack itemInHand, boolean isShiftClick) {
        int amount = isShiftClick ? itemInHand.getAmount() : 1;

        // Check if bank has enough diamonds
        if (plugin.getDiamondBankManager().getTotalDiamonds() < amount) {
            int available = plugin.getDiamondBankManager().getTotalDiamonds();
            player.sendMessage(Component.text("The bank only has " + available + " diamond(s) available! Cannot convert " + amount + " coin(s).", NamedTextColor.RED));
            return;
        }

        // Remove diamonds from bank
        if (!plugin.getDiamondBankManager().removeDiamonds(amount)) {
            player.sendMessage(Component.text("The bank has no diamonds available! Cannot convert coins right now.", NamedTextColor.RED));
            return;
        }

        // Remove coins from hand
        itemInHand.setAmount(itemInHand.getAmount() - amount);

        // Give diamonds to player
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, amount));

        // Log transaction
        plugin.getTransactionLogger().logTransaction(player, -amount, amount);

        // Send message to player
        if (amount == 1) {
            player.sendMessage(Component.text("Successfully sold 1 coin for 1 diamond!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Successfully sold " + amount + " coins for " + amount + " diamonds!", NamedTextColor.GREEN));
        }
    }

    /**
     * Handle bank NPC interaction
     */
    public void handleBankNPC(Player player) {
        // Check if player has bank manager permission
        if (!player.hasPermission("smpcurrency.bank.manage")) {
            player.sendMessage(Component.text("You don't have permission to access the diamond bank!", NamedTextColor.RED));
            return;
        }

        // Get total diamonds in bank
        int totalDiamonds = plugin.getDiamondBankManager().getTotalDiamonds();

        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Diamond Bank", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("Total Diamonds: " + totalDiamonds, NamedTextColor.WHITE));
        player.sendMessage(Component.text("═══════════════════════════", NamedTextColor.GOLD));
        player.sendMessage(Component.text("Use /bank withdraw <amount> to withdraw diamonds", NamedTextColor.GRAY));
    }

    /**
     * Check if an item is the same as a coin (for comparison)
     */
    private boolean isSameCoin(ItemStack item, ItemStack coin) {
        if (item == null || item.getType() != Material.PAPER) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta coinMeta = coin.getItemMeta();

        if (itemMeta == null || coinMeta == null) {
            return false;
        }

        // Check if both have the same item model
        try {
            NamespacedKey itemModel = itemMeta.getItemModel();
            NamespacedKey coinModel = coinMeta.getItemModel();

            if (itemModel != null && coinModel != null) {
                return itemModel.equals(coinModel);
            }
        } catch (Exception e) {
            // Fallback comparison
        }

        // Fallback: compare display names
        Component itemName = itemMeta.displayName();
        Component coinName = coinMeta.displayName();

        if (itemName != null && coinName != null) {
            return itemName.equals(coinName);
        }

        return false;
    }

    /**
     * Get all registered NPC IDs of a certain type
     */
    public Map<Integer, NPCType> getAllNPCs() {
        return new HashMap<>(npcTypes);
    }

    /**
     * Save NPC registrations to config
     */
    public void save() {
        plugin.getConfig().set("npcs.exchange", null);
        plugin.getConfig().set("npcs.bank", null);

        for (Map.Entry<Integer, NPCType> entry : npcTypes.entrySet()) {
            if (entry.getValue() == NPCType.EXCHANGE) {
                plugin.getConfig().set("npcs.exchange." + entry.getKey(), true);
            } else if (entry.getValue() == NPCType.BANK) {
                plugin.getConfig().set("npcs.bank." + entry.getKey(), true);
            }
        }
        plugin.saveConfig();
    }

    /**
     * Load NPC registrations from config
     */
    public void load() {
        npcTypes.clear();

        if (plugin.getConfig().contains("npcs.exchange")) {
            for (String key : plugin.getConfig().getConfigurationSection("npcs.exchange").getKeys(false)) {
                try {
                    int npcId = Integer.parseInt(key);
                    npcTypes.put(npcId, NPCType.EXCHANGE);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid NPC ID in config: " + key);
                }
            }
        }

        if (plugin.getConfig().contains("npcs.bank")) {
            for (String key : plugin.getConfig().getConfigurationSection("npcs.bank").getKeys(false)) {
                try {
                    int npcId = Integer.parseInt(key);
                    npcTypes.put(npcId, NPCType.BANK);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid NPC ID in config: " + key);
                }
            }
        }

        plugin.getLogger().info("Loaded " + npcTypes.size() + " NPC(s) from config");
    }
}
