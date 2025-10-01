package me.shreyjain.smpCurrency.managers;

import me.shreyjain.smpCurrency.SmpCurrency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Manages configured coin (currency) item definitions loaded from config.yml.
 */
public class CoinManager {
    private final SmpCurrency plugin;
    private final Map<String, CoinType> coins = new HashMap<>();

    public CoinManager(SmpCurrency plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        coins.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("coins");
        if (root == null) {
            plugin.getLogger().warning("No 'coins' section found in config.yml");
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;
            String name = sec.getString("name", id);
            String matName = sec.getString("material", "PAPER").toUpperCase(Locale.ROOT);
            Material material = Material.matchMaterial(matName);
            if (material == null) {
                plugin.getLogger().warning("Invalid material for coin '" + id + "': " + matName + " (defaulting to PAPER)");
                material = Material.PAPER;
            }
            List<String> loreLines = sec.getStringList("lore");
            CoinType type = new CoinType(id, name, material, loreLines);
            coins.put(id.toLowerCase(Locale.ROOT), type);
        }
        plugin.getLogger().info("Loaded " + coins.size() + " coin definition(s)");
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    public Set<String> getCoinIds() {
        return Collections.unmodifiableSet(coins.keySet());
    }

    public Optional<CoinType> getCoinType(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(coins.get(id.toLowerCase(Locale.ROOT)));
    }

    /**
     * Creates an ItemStack for the given coin id and amount.
     */
    public ItemStack createCoinItem(String id, int amount) {
        CoinType type = getCoinType(id).orElse(null);
        if (type == null) {
            ItemStack stack = new ItemStack(Material.PAPER, Math.max(1, Math.min(64, amount)));
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("Unknown Coin").color(NamedTextColor.RED));
                meta.setItemModel(NamespacedKey.fromString("currency:coin"));
                stack.setItemMeta(meta);
            }
            return stack;
        }
        int amt = Math.max(1, Math.min(64, amount));
        ItemStack stack = new ItemStack(type.material(), amt);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(parseComponent(type.name()));
            List<Component> lore = new ArrayList<>();
            for (String line : type.lore()) lore.add(parseComponent(line));
            if (!lore.isEmpty()) meta.lore(lore);
            // Set model to currency:<id>
            meta.setItemModel(NamespacedKey.fromString("currency:" + type.id()));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private Component parseComponent(String legacy) {
        if (legacy == null) return Component.empty();
        String colored = ChatColor.translateAlternateColorCodes('&', legacy);
        return LegacyComponentSerializer.legacySection().deserialize(colored);
    }

    public record CoinType(String id, String name, Material material, List<String> lore) {}
}
