package me.shreyjain.smpCurrency.managers;

import me.shreyjain.smpCurrency.SmpCurrency;
import me.shreyjain.smpCurrency.items.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Manages company creation, director assignments, and share issuance.
 */
public class CompanyManager {
    private final SmpCurrency plugin;
    private final Map<String, Company> companies = new HashMap<>();
    private File companiesFile;
    private FileConfiguration companiesConfig;

    private static final Pattern COMPANY_ID_PATTERN = Pattern.compile("[A-Z0-9]{2,8}");

    public CompanyManager(SmpCurrency plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        companies.clear();
        companiesFile = new File(plugin.getDataFolder(), "companies.yml");
        if (!companiesFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                companiesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create companies.yml: " + e.getMessage());
            }
        }
        companiesConfig = YamlConfiguration.loadConfiguration(companiesFile);
        ConfigurationSection root = companiesConfig.getConfigurationSection("companies");
        if (root != null) {
            for (String id : root.getKeys(false)) {
                ConfigurationSection sec = root.getConfigurationSection(id);
                if (sec == null) continue;
                String name = sec.getString("name", id);
                List<String> directorStrs = sec.getStringList("directors");
                Set<UUID> directors = new HashSet<>();
                for (String s : directorStrs) {
                    try { directors.add(UUID.fromString(s)); } catch (IllegalArgumentException ignored) {}
                }
                int totalShares = sec.getInt("totalShares", 0);
                String ticker = id.toUpperCase(Locale.ROOT);
                companies.put(ticker.toLowerCase(Locale.ROOT), new Company(ticker, name, directors, totalShares));
            }
        }
        plugin.getLogger().info("Loaded " + companies.size() + " companies");
    }

    public synchronized void save() {
        if (companiesConfig == null) return;
        companiesConfig.set("companies", null); // clear
        for (Company c : companies.values()) {
            String path = "companies." + c.id();
            companiesConfig.set(path + ".name", c.name());
            List<String> directors = c.directors().stream().map(UUID::toString).toList();
            companiesConfig.set(path + ".directors", directors);
            companiesConfig.set(path + ".totalShares", c.totalShares());
        }
        try {
            companiesConfig.save(companiesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save companies.yml: " + e.getMessage());
        }
    }

    public boolean isValidCompanyId(String id) {
        return id != null && COMPANY_ID_PATTERN.matcher(id.toUpperCase(Locale.ROOT)).matches();
    }

    public Optional<Company> getCompany(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(companies.get(id.toLowerCase(Locale.ROOT)));
    }

    public boolean companyExists(String id) { return getCompany(id).isPresent(); }

    public Company createCompany(String id, String name, UUID creator) {
        String ticker = id.toUpperCase(Locale.ROOT);
        Company company = new Company(ticker, (name == null || name.isBlank()) ? ticker : name, new HashSet<>(), 0);
        company.directors().add(creator);
        companies.put(ticker.toLowerCase(Locale.ROOT), company);
        save();
        return company;
    }

    public boolean isDirector(String companyId, UUID user) {
        return getCompany(companyId).map(c -> c.directors().contains(user)).orElse(false);
    }

    public int issueShares(String companyId, UUID director, Player recipient, int amount) {
        Company company = getCompany(companyId).orElse(null);
        if (company == null) return 0;
        if (!company.directors().contains(director)) return 0;
        if (amount <= 0) return 0;
        ConfigurationSection shareDisplay = plugin.getConfig().getConfigurationSection("shares.display");
        String nameTemplate = shareDisplay != null ? shareDisplay.getString("name", "%ticker% Share") : "%ticker% Share";
        java.util.List<String> loreTemplate = shareDisplay != null ? shareDisplay.getStringList("lore") : java.util.List.of("Company: %name%","Ticker: %ticker%");
        int issued = 0;
        for (int i = 0; i < amount; i++) {
            ItemStack stack = me.shreyjain.smpCurrency.items.CustomItems.createShare(company.id(), company.name(), nameTemplate, loreTemplate);
            Map<Integer, ItemStack> overflow = recipient.getInventory().addItem(stack);
            overflow.values().forEach(it -> recipient.getWorld().dropItemNaturally(recipient.getLocation(), it));
            issued++;
        }
        company.totalShares(company.totalShares() + issued);
        save();
        return issued;
    }

    public List<String> listCompanyIds() {
        java.util.List<String> list = new ArrayList<>();
        for (Company c : companies.values()) list.add(c.id());
        list.sort(String::compareTo);
        return list;
    }

    /** Immutable data record with mutable fields via setters for totalShares */
    public static class Company {
        private final String id;
        private final String name;
        private final Set<UUID> directors;
        private int totalShares;

        public Company(String id, String name, Set<UUID> directors, int totalShares) {
            this.id = id;
            this.name = name;
            this.directors = directors;
            this.totalShares = totalShares;
        }
        public String id() { return id; }
        public String name() { return name; }
        public Set<UUID> directors() { return directors; }
        public int totalShares() { return totalShares; }
        public void totalShares(int v) { this.totalShares = v; }
    }
}
