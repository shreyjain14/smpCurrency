package me.shreyjain.smpCurrency.managers;

import me.shreyjain.smpCurrency.SmpCurrency;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Manages the diamond bank storage
 */
public class DiamondBankManager {
    private final SmpCurrency plugin;
    private final File bankFile;
    private FileConfiguration bankConfig;

    public DiamondBankManager(SmpCurrency plugin) {
        this.plugin = plugin;
        this.bankFile = new File(plugin.getDataFolder(), "diamond_bank.yml");
        load();
    }

    private void load() {
        if (!bankFile.exists()) {
            try {
                bankFile.getParentFile().mkdirs();
                bankFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create diamond bank file: " + e.getMessage());
            }
        }
        bankConfig = YamlConfiguration.loadConfiguration(bankFile);
        if (!bankConfig.contains("total-diamonds")) {
            bankConfig.set("total-diamonds", 0);
            save();
        }
    }

    public void save() {
        try {
            bankConfig.save(bankFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save diamond bank data: " + e.getMessage());
        }
    }

    /**
     * Add diamonds to the bank
     */
    public void addDiamonds(int amount) {
        int current = bankConfig.getInt("total-diamonds", 0);
        bankConfig.set("total-diamonds", current + amount);
        save();
    }

    /**
     * Remove diamonds from the bank
     * @return true if successful, false if not enough diamonds
     */
    public boolean removeDiamonds(int amount) {
        int current = bankConfig.getInt("total-diamonds", 0);
        if (current < amount) {
            return false;
        }
        bankConfig.set("total-diamonds", current - amount);
        save();
        return true;
    }

    /**
     * Get total diamonds in bank
     */
    public int getTotalDiamonds() {
        return bankConfig.getInt("total-diamonds", 0);
    }
}

