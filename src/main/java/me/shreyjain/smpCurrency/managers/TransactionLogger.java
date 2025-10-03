package me.shreyjain.smpCurrency.managers;

import me.shreyjain.smpCurrency.SmpCurrency;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logs all diamond-to-coin transactions
 */
public class TransactionLogger {
    private final SmpCurrency plugin;
    private final File logFile;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TransactionLogger(SmpCurrency plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "transactions.log");

        if (!logFile.exists()) {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create transaction log file: " + e.getMessage());
            }
        }
    }

    /**
     * Log a diamond-to-coin transaction
     * Supports both directions: positive for diamond->coin, negative for coin->diamond
     */
    public void logTransaction(Player player, int diamondAmount, int coinAmount) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry;

        if (diamondAmount > 0) {
            // Diamond to coin exchange
            logEntry = String.format("[%s] %s (UUID: %s) exchanged %d diamond(s) for %d coin(s)%n",
                    timestamp, player.getName(), player.getUniqueId(), diamondAmount, coinAmount);
        } else {
            // Coin to diamond exchange (sell)
            logEntry = String.format("[%s] %s (UUID: %s) sold %d coin(s) for %d diamond(s)%n",
                    timestamp, player.getName(), player.getUniqueId(), Math.abs(coinAmount), Math.abs(diamondAmount));
        }

        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(logEntry);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write transaction log: " + e.getMessage());
        }
    }

    /**
     * Log a bank withdrawal
     */
    public void logBankWithdrawal(Player player, int diamondAmount) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] %s (UUID: %s) withdrew %d diamond(s) from bank%n",
                timestamp, player.getName(), player.getUniqueId(), diamondAmount);

        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(logEntry);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write transaction log: " + e.getMessage());
        }
    }

    /**
     * Log a bank deposit
     */
    public void logBankDeposit(Player player, int diamondAmount) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] %s (UUID: %s) deposited %d diamond(s) to bank%n",
                timestamp, player.getName(), player.getUniqueId(), diamondAmount);

        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(logEntry);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write transaction log: " + e.getMessage());
        }
    }
}
