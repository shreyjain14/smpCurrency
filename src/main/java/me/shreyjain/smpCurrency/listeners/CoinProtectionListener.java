package me.shreyjain.smpCurrency.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Prevents coins from being stored in ender chests or shulker boxes
 */
public class CoinProtectionListener implements Listener {
    private static final NamespacedKey COIN_MODEL_KEY = NamespacedKey.fromString("currency:coin");

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        Inventory topInventory = event.getView().getTopInventory();

        // Check if top inventory is restricted
        boolean topIsRestricted = topInventory.getType() == InventoryType.ENDER_CHEST || isShulkerBox(topInventory);

        if (!topIsRestricted) {
            return;
        }

        // Case 1: Direct click in ender chest/shulker with coin on cursor
        if (clickedInventory.equals(topInventory) && isCoinItem(cursorItem)) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Coins cannot be stored in ender chests or shulker boxes!", NamedTextColor.RED));
            return;
        }

        // Case 2: Clicking a coin already in the restricted inventory
        if (clickedInventory.equals(topInventory) && isCoinItem(currentItem)) {
            // Allow taking out coins, but prevent swapping with another coin
            if (isCoinItem(cursorItem)) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Coins cannot be stored in ender chests or shulker boxes!", NamedTextColor.RED));
            }
            return;
        }

        // Case 3: Shift-clicking from player inventory into restricted inventory
        if (event.isShiftClick() && !clickedInventory.equals(topInventory)) {
            if (isCoinItem(currentItem)) {
                event.setCancelled(true);
                player.sendMessage(Component.text("Coins cannot be stored in ender chests or shulker boxes!", NamedTextColor.RED));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();

        // Check if top inventory is restricted
        boolean topIsRestricted = topInventory.getType() == InventoryType.ENDER_CHEST || isShulkerBox(topInventory);

        if (!topIsRestricted) {
            return;
        }

        // Check if dragging coins
        if (!isCoinItem(event.getOldCursor())) {
            return;
        }

        // Check if any of the drag slots are in the restricted inventory
        int topSize = topInventory.getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                // Dragging into restricted inventory
                event.setCancelled(true);
                player.sendMessage(Component.text("Coins cannot be stored in ender chests or shulker boxes!", NamedTextColor.RED));
                return;
            }
        }
    }

    /**
     * Check if an item is a coin
     */
    private boolean isCoinItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // Check if item has the coin model key
        try {
            NamespacedKey itemModel = meta.getItemModel();
            if (itemModel != null && itemModel.equals(COIN_MODEL_KEY)) {
                return true;
            }
        } catch (Exception e) {
            // Fallback: check if it's paper with "Coin" in the display name
            Component displayName = meta.displayName();
            if (displayName != null && item.getType() == Material.PAPER) {
                String plainText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(displayName);
                return plainText.contains("Coin");
            }
        }

        return false;
    }

    /**
     * Check if an inventory is a shulker box
     */
    private boolean isShulkerBox(Inventory inventory) {
        if (inventory.getHolder() instanceof org.bukkit.block.ShulkerBox) {
            return true;
        }

        // Additional check for shulker box inventory type
        return inventory.getType().name().contains("SHULKER");
    }
}
