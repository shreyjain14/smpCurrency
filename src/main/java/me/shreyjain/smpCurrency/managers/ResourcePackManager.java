package me.shreyjain.smpCurrency.managers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.shreyjain.smpCurrency.SmpCurrency;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackManager {

    private final SmpCurrency plugin;
    private final Set<UUID> pendingPlayers;
    private String resourcePackUrl;
    private String resourcePackHash;
    private HttpServer httpServer;
    private boolean hostLocally;
    private int port;

    public ResourcePackManager(SmpCurrency plugin) {
        this.plugin = plugin;
        this.pendingPlayers = new HashSet<>();
        loadResourcePackConfig();
        if (hostLocally) {
            startLocalServer();
        }
    }

    private void loadResourcePackConfig() {
        this.hostLocally = plugin.getConfig().getBoolean("resource-pack.host-locally", true);
        this.port = plugin.getConfig().getInt("resource-pack.port", 8080);
        this.resourcePackUrl = plugin.getConfig().getString("resource-pack.url", "");
        this.resourcePackHash = plugin.getConfig().getString("resource-pack.sha1", "");

        if (hostLocally) {
            this.resourcePackUrl = "http://localhost:" + port + "/currency-pack.zip";
        } else if (resourcePackUrl.isEmpty()) {
            plugin.getLogger().warning("No resource pack URL configured and local hosting is disabled!");
        }
    }

    private void startLocalServer() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            httpServer.createContext("/currency-pack.zip", new ResourcePackHandler());
            httpServer.setExecutor(null);
            httpServer.start();
            plugin.getLogger().info("Started local resource pack server on port " + port);

            // Generate the resource pack on startup
            generateResourcePack();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start local resource pack server: " + e.getMessage());
            hostLocally = false;
        }
    }

    private void generateResourcePack() {
        try {
            Path packPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "currency-pack.zip");
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(packPath))) {
                addPackMcMeta(zos);
                addBasicCoinResourcePack(zos);
                addStockResourcePack(zos); // new stock assets
            }
            this.resourcePackHash = calculateSHA1(packPath);
            plugin.getLogger().info("Generated resource pack with SHA1: " + resourcePackHash);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to generate resource pack: " + e.getMessage());
        }
    }

    private void addPackMcMeta(ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry("pack.mcmeta");
        zos.putNextEntry(entry);

        String packMcMeta = "{\n" +
            "  \"pack\": {\n" +
            "    \"description\": \"Custom Currency Items for ThrowbackSMP\\nIncludes coins and certificates with custom textures\",\n" +
            "    \"pack_format\": 64\n" +
            "  }\n" +
            "}";

        zos.write(packMcMeta.getBytes());
        zos.closeEntry();
    }

    private void addBasicCoinResourcePack(ZipOutputStream zos) throws IOException {
        // Add basic coin item definition (1.21.7 format)
        ZipEntry coinItemEntry = new ZipEntry("assets/currency/items/coin.json");
        zos.putNextEntry(coinItemEntry);
        String coinItem = "{\n" +
            "    \"model\": {\n" +
            "        \"type\": \"minecraft:model\",\n" +
            "        \"model\": \"currency:item/coin\"\n" +
            "    }\n" +
            "}";
        zos.write(coinItem.getBytes());
        zos.closeEntry();

        // Add coin model
        ZipEntry coinModelEntry = new ZipEntry("assets/currency/models/item/coin.json");
        zos.putNextEntry(coinModelEntry);
        String coinModel = "{\n" +
            "  \"parent\": \"minecraft:item/generated\",\n" +
            "  \"textures\": {\n" +
            "    \"layer0\": \"currency:item/coin\"\n" +
            "  }\n" +
            "}";
        zos.write(coinModel.getBytes());
        zos.closeEntry();

        // Add the actual coin texture from resources/assets
        addCoinTexture(zos);
    }

    private void addCoinTexture(ZipOutputStream zos) throws IOException {
        try {
            // Load the coin.png from resources/assets
            InputStream textureStream = plugin.getResource("assets/coin.png");
            if (textureStream != null) {
                ZipEntry textureEntry = new ZipEntry("assets/currency/textures/item/coin.png");
                zos.putNextEntry(textureEntry);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = textureStream.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                }
                textureStream.close();
                zos.closeEntry();

                plugin.getLogger().info("Added coin texture to resource pack");
            } else {
                plugin.getLogger().warning("Could not find coin.png in resources/assets - texture will not display");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to add coin texture: " + e.getMessage());
        }
    }

    private void addStockResourcePack(ZipOutputStream zos) throws IOException {
        // Item definition
        ZipEntry stockItemEntry = new ZipEntry("assets/currency/items/stock.json");
        zos.putNextEntry(stockItemEntry);
        String stockItem = "{\n" +
            "    \"model\": {\n" +
            "        \"type\": \"minecraft:model\",\n" +
            "        \"model\": \"currency:item/stock\"\n" +
            "    }\n" +
            "}";
        zos.write(stockItem.getBytes());
        zos.closeEntry();

        // Model file
        ZipEntry stockModelEntry = new ZipEntry("assets/currency/models/item/stock.json");
        zos.putNextEntry(stockModelEntry);
        String stockModel = "{\n" +
            "  \"parent\": \"minecraft:item/generated\",\n" +
            "  \"textures\": {\n" +
            "    \"layer0\": \"currency:item/stock\"\n" +
            "  }\n" +
            "}";
        zos.write(stockModel.getBytes());
        zos.closeEntry();

        addStockTexture(zos);
    }

    private void addStockTexture(ZipOutputStream zos) throws IOException {
        try (InputStream textureStream = plugin.getResource("assets/stock.png")) { // expect stock.png in resources
            if (textureStream != null) {
                ZipEntry textureEntry = new ZipEntry("assets/currency/textures/item/stock.png");
                zos.putNextEntry(textureEntry);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = textureStream.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                }
                zos.closeEntry();
                plugin.getLogger().info("Added stock texture to resource pack");
            } else {
                plugin.getLogger().warning("stock.png not found in resources/assets. Share items will show missing texture.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to add stock texture: " + e.getMessage());
        }
    }

    private String calculateSHA1(Path filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] hashBytes = digest.digest(fileBytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void promptPlayerForResourcePack(Player player) {
        if (resourcePackUrl.isEmpty()) {
            return;
        }

        // Automatically apply the resource pack without prompting
        applyResourcePack(player, false);
    }

    public void applyResourcePack(Player player, boolean showMessages) {
        if (resourcePackUrl.isEmpty()) {
            if (showMessages) {
                player.sendMessage(ChatColor.RED + "No resource pack is configured on this server.");
            }
            return;
        }

        try {
            if (resourcePackHash.isEmpty()) {
                player.setResourcePack(resourcePackUrl);
            } else {
                player.setResourcePack(resourcePackUrl, resourcePackHash);
            }

            if (showMessages) {
                player.sendMessage(ChatColor.GREEN + "Applying custom currency texture pack...");
                player.sendMessage(ChatColor.YELLOW + "Make sure to accept the resource pack prompt!");
            }

            pendingPlayers.remove(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply resource pack to " + player.getName() + ": " + e.getMessage());
            if (showMessages) {
                player.sendMessage(ChatColor.RED + "Failed to apply resource pack. Please try again later.");
            }
        }
    }

    public void declineResourcePack(Player player) {
        pendingPlayers.remove(player.getUniqueId());
        player.sendMessage(ChatColor.YELLOW + "Resource pack declined. You can use " +
            ChatColor.GOLD + "/currency texturepack" + ChatColor.YELLOW + " to apply it later.");
        player.sendMessage(ChatColor.GRAY + "Note: Without the texture pack, custom coins will appear as regular items.");
    }

    public boolean isPending(Player player) {
        return pendingPlayers.contains(player.getUniqueId());
    }

    public void reloadConfig() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }

        loadResourcePackConfig();

        if (hostLocally) {
            startLocalServer();
        }
    }

    public boolean isConfigured() {
        return !resourcePackUrl.isEmpty();
    }

    public void shutdown() {
        if (httpServer != null) {
            httpServer.stop(0);
            plugin.getLogger().info("Stopped local resource pack server");
        }
    }

    public String getPackInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resource pack URL: ").append(resourcePackUrl == null || resourcePackUrl.isEmpty() ? "(not configured)" : resourcePackUrl).append('\n');
        sb.append("SHA1: ").append(resourcePackHash == null || resourcePackHash.isEmpty() ? "(not set)" : resourcePackHash).append('\n');

        Path packPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "currency-pack.zip");
        if (Files.exists(packPath)) {
            sb.append("Pack exists at: ").append(packPath.toString()).append('\n');
            sb.append("Contents:\n");
            try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(packPath.toFile())) {
                java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zf.entries();
                while (entries.hasMoreElements()) {
                    java.util.zip.ZipEntry e = entries.nextElement();
                    sb.append(" - ").append(e.getName()).append('\n');
                }
            } catch (IOException e) {
                sb.append("Failed to read zip: ").append(e.getMessage()).append('\n');
            }
        } else {
            sb.append("Resource pack not generated yet: ").append(packPath.toString()).append('\n');
        }

        return sb.toString();
    }

    private class ResourcePackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Path packPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "currency-pack.zip");

            if (Files.exists(packPath)) {
                byte[] response = Files.readAllBytes(packPath);
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"currency-pack.zip\"");
                exchange.sendResponseHeaders(200, response.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } else {
                String response = "Resource pack not found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }
}
