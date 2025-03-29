package com.example.webserverplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class WebServerPlugin extends JavaPlugin {
    private WebServer webServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int port = getConfig().getInt("port", 8080);
        String htmlDirectory = getConfig().getString("html-directory", "html");

        try {
            webServer = new WebServer(this, port, htmlDirectory);
            webServer.start();
            getLogger().info("Web服务器开在了端口: " + port);
        } catch (Exception e) {
            getLogger().severe("Web服务器启动失败: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (webServer != null) {
            webServer.stop();
            getLogger().info("Web服务器停止");
        }
    }
}