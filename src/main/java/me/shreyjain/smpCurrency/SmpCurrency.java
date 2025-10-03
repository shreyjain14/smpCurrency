package me.shreyjain.smpCurrency;

import me.shreyjain.smpCurrency.commands.CurrencyCommand;
import me.shreyjain.smpCurrency.commands.CompanyCommand;
import me.shreyjain.smpCurrency.commands.BankCommand;
import me.shreyjain.smpCurrency.listeners.PlayerJoinListener;
import me.shreyjain.smpCurrency.listeners.ShareUpdateListener;
import me.shreyjain.smpCurrency.listeners.NPCClickListener;
import me.shreyjain.smpCurrency.listeners.CoinProtectionListener;
import me.shreyjain.smpCurrency.managers.CoinManager;
import me.shreyjain.smpCurrency.managers.CompanyManager;
import me.shreyjain.smpCurrency.managers.ResourcePackManager;
import me.shreyjain.smpCurrency.managers.NPCManager;
import me.shreyjain.smpCurrency.managers.DiamondBankManager;
import me.shreyjain.smpCurrency.managers.TransactionLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmpCurrency extends JavaPlugin {

    private static SmpCurrency instance;
    private CoinManager coinManager;
    private ResourcePackManager resourcePackManager;
    private CompanyManager companyManager;
    private NPCManager npcManager;
    private DiamondBankManager diamondBankManager;
    private TransactionLogger transactionLogger;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.coinManager = new CoinManager(this);
        this.resourcePackManager = new ResourcePackManager(this);
        this.companyManager = new CompanyManager(this);
        this.npcManager = new NPCManager(this);
        this.diamondBankManager = new DiamondBankManager(this);
        this.transactionLogger = new TransactionLogger(this);

        // Load NPC registrations
        this.npcManager.load();

        // Register commands (executor + tab completion with same instance)
        CurrencyCommand currencyCommand = new CurrencyCommand(this);
        if (getCommand("currency") != null) {
            getCommand("currency").setExecutor(currencyCommand);
            getCommand("currency").setTabCompleter(currencyCommand);
        }
        CompanyCommand companyCommand = new CompanyCommand(this);
        if (getCommand("company") != null) {
            getCommand("company").setExecutor(companyCommand);
            getCommand("company").setTabCompleter(companyCommand);
        }
        BankCommand bankCommand = new BankCommand(this);
        if (getCommand("bank") != null) {
            getCommand("bank").setExecutor(bankCommand);
            getCommand("bank").setTabCompleter(bankCommand);
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ShareUpdateListener(this), this);
        getServer().getPluginManager().registerEvents(new CoinProtectionListener(), this);

        // Register NPC listener if Citizens is available
        if (npcManager.isCitizensEnabled()) {
            getServer().getPluginManager().registerEvents(new NPCClickListener(this), this);
            getLogger().info("Citizens integration enabled!");
        } else {
            getLogger().warning("Citizens plugin not found - NPC features will be disabled");
        }

        getLogger().info("SmpCurrency plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Shutdown the HTTP server if running
        if (resourcePackManager != null) {
            resourcePackManager.shutdown();
        }
        if (companyManager != null) {
            companyManager.save();
        }
        if (npcManager != null) {
            npcManager.save();
        }
        getLogger().info("SmpCurrency plugin has been disabled!");
    }

    public static SmpCurrency getInstance() {
        return instance;
    }

    public CoinManager getCoinManager() {
        return coinManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }

    public CompanyManager getCompanyManager() {
        return companyManager;
    }

    public NPCManager getNPCManager() {
        return npcManager;
    }

    public DiamondBankManager getDiamondBankManager() {
        return diamondBankManager;
    }

    public TransactionLogger getTransactionLogger() {
        return transactionLogger;
    }
}
