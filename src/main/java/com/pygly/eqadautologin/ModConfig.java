package com.pygly.eqadautologin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("eqad-autologin-config");
    private static final Path CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".eqad-autologin");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean autoLoginEnabled = true;
    public boolean autoOpenMenuEnabled = false;
    public boolean autoJoinSubServerEnabled = false;
    public String targetSubServer = "sc";

    public static ModConfig load() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (Files.exists(CONFIG_FILE)) {
                String json = Files.readString(CONFIG_FILE);
                ModConfig config = GSON.fromJson(json, ModConfig.class);
                if (config != null) {
                    LOGGER.info("配置已加载: 自动登录={}, 自动打开菜单={}, 自动进入区服={}, 目标区服={}",
                            config.autoLoginEnabled, config.autoOpenMenuEnabled,
                            config.autoJoinSubServerEnabled, config.targetSubServer);
                    return config;
                }
            }
        } catch (Exception e) {
            LOGGER.error("加载配置失败", e);
        }
        ModConfig defaultConfig = new ModConfig();
        defaultConfig.save();
        return defaultConfig;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_FILE, json);
            LOGGER.info("配置已保存: 自动登录={}, 自动打开菜单={}, 自动进入区服={}, 目标区服={}",
                    autoLoginEnabled, autoOpenMenuEnabled, autoJoinSubServerEnabled, targetSubServer);
        } catch (Exception e) {
            LOGGER.error("保存配置失败", e);
        }
    }

    public String getSubServerDisplayName() {
        return switch (targetSubServer) {
            case "sc" -> "纯净服";
            case "cz" -> "创造服";
            case "sc2" -> "粘液服";
            default -> "未知";
        };
    }

    public void cycleSubServer() {
        targetSubServer = switch (targetSubServer) {
            case "sc" -> "cz";
            case "cz" -> "sc2";
            case "sc2" -> "sc";
            default -> "sc";
        };
    }
}