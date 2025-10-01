package me.shreyjain.smpCurrency.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Factory class for creating custom currency-related items backed by the resource pack.
 */
public final class CustomItems {

    private static final NamespacedKey COIN_MODEL_KEY = NamespacedKey.fromString("currency:coin");
    private static final NamespacedKey STOCK_MODEL_KEY = NamespacedKey.fromString("currency:stock");
    private static final NamespacedKey KEY_COMPANY = NamespacedKey.fromString("smpcurrency:company");

    private CustomItems() {}

    /**
     * Create a custom Coin item stack using the resource pack model (assets/currency/items/coin.json).
     * @param amount stack size (clamped 1-64)
     * @return ItemStack representing the custom coin
     */
    public static ItemStack createCoin(int amount) {
        if (amount < 1) amount = 1;
        if (amount > 64) amount = 64;

        ItemStack stack = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack; // Fallback (should not happen)

        meta.setItemModel(COIN_MODEL_KEY);
        meta.displayName(Component.text("Coin", NamedTextColor.GOLD));
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Create a custom Share item stack representing a stock certificate for a company.
     * @param companyId the ID of the company
     * @param amount stack size (clamped 1-64)
     * @return ItemStack representing the custom share
     */
    public static ItemStack createShare(String companyId, int amount) {
        if (amount < 1) amount = 1;
        if (amount > 64) amount = 64;
        ItemStack stack = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        meta.setItemModel(STOCK_MODEL_KEY);
        meta.displayName(Component.text(companyId + " Share", NamedTextColor.AQUA));
        meta.lore(java.util.List.of(
            Component.text("1 Share", NamedTextColor.GRAY),
            Component.text("Company: " + companyId, NamedTextColor.DARK_GRAY)
        ));
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (KEY_COMPANY != null) {
            pdc.set(KEY_COMPANY, PersistentDataType.STRING, companyId);
        }
        stack.setItemMeta(meta);
        return stack;
    }
}
