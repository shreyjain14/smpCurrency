package me.shreyjain.smpCurrency.commands;

import me.shreyjain.smpCurrency.SmpCurrency;
import me.shreyjain.smpCurrency.managers.CompanyManager;
import me.shreyjain.smpCurrency.managers.CompanyManager.Company;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CompanyCommand implements TabExecutor {

    private final SmpCurrency plugin;

    public CompanyCommand(SmpCurrency plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <create|list|info|issue|adddirector>");
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> handleCreate(sender, label, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "issue" -> handleIssue(sender, args);
            case "adddirector" -> handleAddDirector(sender, args);
            default -> sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can create companies.");
            return;
        }
        if (!sender.hasPermission("smpcurrency.company.create")) {
            sender.sendMessage(ChatColor.RED + "You lack permission (smpcurrency.company.create).");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " create <companyId> [display name...]");
            return;
        }
        String id = args[1].toLowerCase(Locale.ROOT);
        CompanyManager cm = plugin.getCompanyManager();
        if (!cm.isValidCompanyId(id)) {
            sender.sendMessage(ChatColor.RED + "Invalid company id. Use lowercase letters, numbers, underscore (2-32 chars).");
            return;
        }
        if (cm.companyExists(id)) {
            sender.sendMessage(ChatColor.RED + "Company already exists.");
            return;
        }
        String display = id;
        if (args.length > 2) {
            display = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }
        Company company = cm.createCompany(id, display, player.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + "Created company '" + company.id() + "' (" + company.name() + ") and you are a director.");
    }

    private void handleList(CommandSender sender) {
        CompanyManager cm = plugin.getCompanyManager();
        List<String> ids = cm.listCompanyIds();
        if (ids.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No companies exist.");
            return;
        }
        sender.sendMessage(ChatColor.GOLD + "Companies: " + ChatColor.YELLOW + String.join(", ", ids));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /company info <id>");
            return;
        }
        String id = args[1];
        CompanyManager cm = plugin.getCompanyManager();
        Company c = cm.getCompany(id).orElse(null);
        if (c == null) {
            sender.sendMessage(ChatColor.RED + "Company not found.");
            return;
        }
        String directors = c.directors().stream().map(uuid -> {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            return op.getName() != null ? op.getName() : uuid.toString().substring(0, 8);
        }).collect(Collectors.joining(", "));
        sender.sendMessage(ChatColor.AQUA + "Company: " + c.id());
        sender.sendMessage(ChatColor.AQUA + "Name: " + ChatColor.WHITE + c.name());
        sender.sendMessage(ChatColor.AQUA + "Directors: " + ChatColor.WHITE + (directors.isBlank() ? "(none)" : directors));
        sender.sendMessage(ChatColor.AQUA + "Total Shares Issued: " + ChatColor.WHITE + c.totalShares());
    }

    private void handleIssue(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can issue shares.");
            return;
        }
        if (!sender.hasPermission("smpcurrency.company.issue")) {
            sender.sendMessage(ChatColor.RED + "You lack permission (smpcurrency.company.issue).");
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /company issue <companyId> <player> <amount>");
            return;
        }
        String companyId = args[1];
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
            return;
        }
        int amount;
        try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount.");
            return;
        }
        if (amount <= 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be > 0.");
            return;
        }
        CompanyManager cm = plugin.getCompanyManager();
        if (!cm.isDirector(companyId, player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You are not a director of that company.");
            return;
        }
        int issued = cm.issueShares(companyId, player.getUniqueId(), target, amount);
        if (issued <= 0) {
            sender.sendMessage(ChatColor.RED + "Failed to issue shares.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Issued " + issued + " shares of '" + companyId + "' to " + target.getName());
            target.sendMessage(ChatColor.GREEN + "You received " + issued + " shares of '" + companyId + "'.");
        }
    }

    private void handleAddDirector(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can add directors.");
            return;
        }
        if (!sender.hasPermission("smpcurrency.company.issue")) { // reuse issue perm for director management
            sender.sendMessage(ChatColor.RED + "You lack permission.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /company adddirector <companyId> <player>");
            return;
        }
        String companyId = args[1];
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
            return;
        }
        CompanyManager cm = plugin.getCompanyManager();
        Company company = cm.getCompany(companyId).orElse(null);
        if (company == null) {
            sender.sendMessage(ChatColor.RED + "Company not found.");
            return;
        }
        if (!company.directors().contains(player.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "You must be an existing director to add another.");
            return;
        }
        if (company.directors().add(target.getUniqueId())) {
            cm.save();
            sender.sendMessage(ChatColor.GREEN + "Added director " + target.getName() + " to company '" + company.id() + "'.");
            target.sendMessage(ChatColor.GREEN + "You are now a director of company '" + company.id() + "'.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + target.getName() + " is already a director.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        CompanyManager cm = plugin.getCompanyManager();
        if (args.length == 1) {
            return Arrays.asList("create", "list", "info", "issue", "adddirector").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "info", "issue", "adddirector" -> {
                    String prefix = args[1].toLowerCase(Locale.ROOT);
                    return cm.listCompanyIds().stream().filter(id -> id.startsWith(prefix)).sorted().collect(Collectors.toList());
                }
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("issue")) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix)).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("adddirector")) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix)).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

