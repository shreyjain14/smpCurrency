package me.shreyjain.smpCurrency.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
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
    private static final NamespacedKey KEY_SHARE_ID = NamespacedKey.fromString("smpcurrency:share_id");

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
     * @return ItemStack representing the custom share
     */
    public static ItemStack createShare(String companyId) {
        ItemStack stack = new ItemStack(Material.PAPER, 1);
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
        if (KEY_SHARE_ID != null) {
            pdc.set(KEY_SHARE_ID, PersistentDataType.STRING, java.util.UUID.randomUUID().toString());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Create a custom Share item stack with provided templates.
     * @param ticker uppercase company ticker
     * @param companyName display name of company
     * @param nameTemplate template supporting %ticker% %name%
     * @param loreTemplate lore lines supporting same placeholders
     */
    public static ItemStack createShare(String ticker, String companyName, String nameTemplate, java.util.List<String> loreTemplate) {
        ItemStack stack = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;
        meta.setItemModel(STOCK_MODEL_KEY);
        meta.displayName(legacyToComponent(applyPlaceholders(nameTemplate, ticker, companyName)));
        if (loreTemplate != null && !loreTemplate.isEmpty()) {
            java.util.List<net.kyori.adventure.text.Component> lore = new java.util.ArrayList<>();
            for (String line : loreTemplate) {
                lore.add(legacyToComponent(applyPlaceholders(line, ticker, companyName)));
            }
            meta.lore(lore);
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (KEY_COMPANY != null) pdc.set(KEY_COMPANY, PersistentDataType.STRING, ticker);
        if (KEY_SHARE_ID != null) pdc.set(KEY_SHARE_ID, PersistentDataType.STRING, java.util.UUID.randomUUID().toString());
        stack.setItemMeta(meta);
        return stack;
    }

    private static String applyPlaceholders(String template, String ticker, String companyName) {
        if (template == null) return "";
        return template.replace("%ticker%", ticker).replace("%name%", companyName);
    }

    private static net.kyori.adventure.text.Component legacyToComponent(String legacy) {
        if (legacy == null) return net.kyori.adventure.text.Component.empty();
        String colored = ChatColor.translateAlternateColorCodes('&', legacy);
        return LegacyComponentSerializer.legacySection().deserialize(colored);
    }
}
