package me.shreyjain.smpCurrency.listeners;

import me.shreyjain.smpCurrency.SmpCurrency;
import me.shreyjain.smpCurrency.managers.NPCManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Handles NPC right-click interactions for currency exchange and bank
 */
public class NPCClickListener implements Listener {
    private final SmpCurrency plugin;

    public NPCClickListener(SmpCurrency plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        int npcId = event.getNPC().getId();

        NPCManager npcManager = plugin.getNPCManager();
        if (!npcManager.isRegisteredNPC(npcId)) {
            return;
        }

        NPCManager.NPCType type = npcManager.getNPCType(npcId);
        if (type == null) {
            return;
        }

        // Check if player is shift-clicking
        boolean isShiftClick = player.isSneaking();

        switch (type) {
            case EXCHANGE:
                npcManager.handleExchangeNPC(player, isShiftClick);
                break;
            case BANK:
                npcManager.handleBankNPC(player);
                break;
        }
    }
}
