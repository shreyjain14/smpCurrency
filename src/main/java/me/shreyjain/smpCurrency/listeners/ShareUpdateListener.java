package me.shreyjain.smpCurrency.listeners;

import me.shreyjain.smpCurrency.SmpCurrency;
import me.shreyjain.smpCurrency.items.CustomItems;
import me.shreyjain.smpCurrency.managers.CompanyManager;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ShareUpdateListener implements Listener {

    private final SmpCurrency plugin;

    public ShareUpdateListener(SmpCurrency plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        refreshIfShare(event.getItemDrop());
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        refreshIfShare(event.getEntity());
    }

    private void refreshIfShare(Item itemEntity) {
        ItemStack stack = itemEntity.getItemStack();
        if (stack == null || stack.getType() != Material.PAPER) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(CustomItems.KEY_COMPANY, PersistentDataType.STRING)) return; // not a share
        String ticker = pdc.get(CustomItems.KEY_COMPANY, PersistentDataType.STRING);
        if (ticker == null) return;
        CompanyManager cm = plugin.getCompanyManager();
        String companyName = cm.getCompany(ticker).map(CompanyManager.Company::name).orElse(ticker);
        // Refresh using current config templates
        CustomItems.refreshShareDisplay(plugin, stack, ticker, companyName);
        itemEntity.setItemStack(stack); // ensure entity holds updated stack
    }
}

