package me.shreyjain.smpCurrency;

import me.shreyjain.smpCurrency.commands.CurrencyCommand;
import me.shreyjain.smpCurrency.commands.CompanyCommand;
import me.shreyjain.smpCurrency.listeners.PlayerJoinListener;
import me.shreyjain.smpCurrency.listeners.ShareUpdateListener;
import me.shreyjain.smpCurrency.managers.CoinManager;
import me.shreyjain.smpCurrency.managers.CompanyManager;
import me.shreyjain.smpCurrency.managers.ResourcePackManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmpCurrency extends JavaPlugin {

    private static SmpCurrency instance;
    private CoinManager coinManager;
    private ResourcePackManager resourcePackManager;
    private CompanyManager companyManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.coinManager = new CoinManager(this);
        this.resourcePackManager = new ResourcePackManager(this);
        this.companyManager = new CompanyManager(this);

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

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ShareUpdateListener(this), this);

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
}
