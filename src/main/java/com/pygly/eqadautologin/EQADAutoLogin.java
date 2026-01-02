package com.pygly.eqadautologin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EQADAutoLogin {
    public static final String MOD_ID = "eqad-autologin";
    public static final String CHAT_PREFIX = "§l[EQAD AutoLogin]";

    public static final Set<String> TARGET_SERVERS = Set.of(
            "vanilla.eqad.fun", "vanilla-v6.eqad.fun", "vanilla-1.eqad.fun", "vanilla-2.eqad.fun",
            "v4-1-everfree-cz.20percent.cool", "v4-2-everfree-cz.20percent.cool",
            "v6-1-everfree-cz.20percent.cool", "v6-2-everfree-cz.20percent.cool",
            "v6-sh-ct-everfree-cz.20percent.cool", "v6-sh-cm-everfree-cz.20percent.cool"
    );

    private static final Path MOD_ROOT_DIR = Paths.get(System.getProperty("user.home"), "." + MOD_ID);
    public static final Map<String, String> serverPasswords = new HashMap<>();
    private static SecretKey aesKey;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static int autoLoginDelay = 0;
    private static boolean needAutoLogin = false;
    private static boolean initialCheckDone = false;
    private static ModConfig config;

    private static int autoMenuDelay = 0;
    private static boolean needAutoMenu = false;

    private static int autoSubServerDelay = 0;
    private static boolean needAutoSubServer = false;

    private static boolean hasAutoLoggedInThisSession = false;

    public static ModConfig getConfig() {
        if (config == null) {
            config = ModConfig.load();
        }
        return config;
    }

    public static void registerEvents() {
        getConfig();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            loadKeyAndPassword(client);
            String address = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "";

            if (TARGET_SERVERS.contains(address) && !hasAutoLoggedInThisSession) {
                if (config.autoLoginEnabled) {
                    client.inGameHud.getChatHud().addMessage(Text.literal("§e" + CHAT_PREFIX + " 检测到纯净服,1秒后自动登录..."));
                    autoLoginDelay = 20;
                    needAutoLogin = true;

                    if (config.autoJoinSubServerEnabled) {
                        autoSubServerDelay = 30;
                        needAutoSubServer = true;
                    }

                    if (config.autoOpenMenuEnabled) {
                        autoMenuDelay = 60;
                        needAutoMenu = true;
                    }
                } else {
                    if (config.autoOpenMenuEnabled) {
                        client.inGameHud.getChatHud().addMessage(Text.literal("§e" + CHAT_PREFIX + " 检测到纯净服,即将自动打开服务器菜单..."));
                        autoMenuDelay = 20;
                        needAutoMenu = true;
                    }
                }

                hasAutoLoggedInThisSession = true;
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            hasAutoLoggedInThisSession = false;
            LOGGER.info("玩家已断开连接,重置自动登录状态");
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!initialCheckDone && client.getSession() != null) {
                initialCheckDone = true;
                client.execute(() -> {
                    EQADAutoLogin.loadKeyAndPassword(client);
                    if (!EQADAutoLogin.serverPasswords.containsKey("default") && client.currentScreen == null) {
                        client.setScreen(new PasswordScreen(null));
                    }
                });
            }

            if (autoLoginDelay > 0) {
                autoLoginDelay--;
                if (autoLoginDelay == 0 && needAutoLogin) {
                    performAutoLogin(client);
                    needAutoLogin = false;
                }
            }

            if (autoSubServerDelay > 0) {
                autoSubServerDelay--;
                if (autoSubServerDelay == 0 && needAutoSubServer) {
                    performAutoJoinSubServer(client);
                    needAutoSubServer = false;
                }
            }

            if (autoMenuDelay > 0) {
                autoMenuDelay--;
                if (autoMenuDelay == 0 && needAutoMenu) {
                    performAutoOpenMenu(client);
                    needAutoMenu = false;
                }
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof MultiplayerScreen multiplayerScreen) {

                int width = multiplayerScreen.width;
                int height = multiplayerScreen.height;

                ButtonWidget settingsButton = ButtonWidget.builder(
                                Text.literal("§6" + CHAT_PREFIX + " 设置密码"),
                                (button) -> {
                                    MinecraftClient.getInstance().setScreen(new PasswordScreen(multiplayerScreen));
                                }
                        )
                        .dimensions(5, height - 30, 160, 20)
                        .build();

                ((List) multiplayerScreen.children()).add(settingsButton);
            }
        });
    }

    private static void performAutoLogin(MinecraftClient client) {
        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            if (player == null || player.networkHandler == null) {
                autoLoginDelay = 20;
                needAutoLogin = true;
                return;
            }

            String pwd = decrypt(serverPasswords.getOrDefault("default", ""));

            if (pwd.isEmpty()) {
                client.setScreen(new PasswordScreen(client.currentScreen));
                return;
            }

            performLoginCommand(client, pwd);
        });
    }

    private static void performAutoJoinSubServer(MinecraftClient client) {
        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            if (player == null || player.networkHandler == null) {
                autoSubServerDelay = 10;
                needAutoSubServer = true;
                return;
            }

            try {
                String targetServer = config.targetSubServer;
                String displayName = config.getSubServerDisplayName();
                player.networkHandler.sendCommand("server " + targetServer);
                client.inGameHud.getChatHud().addMessage(Text.literal("§a" + CHAT_PREFIX + " 正在自动进入" + displayName + "..."));
                LOGGER.info("玩家 {} 自动跨服指令已发送: /server {}", player.getName().getString(), targetServer);
            } catch (Exception e) {
                LOGGER.error("发送跨服指令失败!", e);
                client.inGameHud.getChatHud().addMessage(Text.literal("§c" + CHAT_PREFIX + " 自动跨服失败!"));
            }
        });
    }

    private static void performAutoOpenMenu(MinecraftClient client) {
        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            if (player == null || player.networkHandler == null) {
                autoMenuDelay = 20;
                needAutoMenu = true;
                return;
            }

            try {
                player.networkHandler.sendCommand("cd");
                client.inGameHud.getChatHud().addMessage(Text.literal("§a" + CHAT_PREFIX + " 自动打开服务器菜单指令已发送!"));
                LOGGER.info("玩家 {} 自动打开菜单指令已发送", player.getName().getString());
            } catch (Exception e) {
                LOGGER.error("发送打开菜单指令失败!", e);
                client.inGameHud.getChatHud().addMessage(Text.literal("§c" + CHAT_PREFIX + " 自动打开菜单失败!"));
            }
        });
    }

    public static void performLoginCommand(MinecraftClient client, String password) {
        ClientPlayerEntity player = client.player;
        if (player == null || player.networkHandler == null) return;

        try {
            player.networkHandler.sendCommand("l " + password);
            client.inGameHud.getChatHud().addMessage(Text.literal("§a" + CHAT_PREFIX + " 自动登录指令已发送!"));
            LOGGER.info("玩家 {} 登录指令已发送", player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("发送登录指令失败!", e);
            client.inGameHud.getChatHud().addMessage(Text.literal("§c" + CHAT_PREFIX + " 自动登录失败!请手动输入。"));
        }
    }


    private static Path getPlayerConfigDir(MinecraftClient client) {
        String playerName = client.getSession() != null ? client.getSession().getUsername() : "unknown";
        return MOD_ROOT_DIR.resolve(playerName);
    }

    private static Path getPasswordFile(MinecraftClient client) {
        return getPlayerConfigDir(client).resolve("password.json");
    }

    private static Path getKeyFile(MinecraftClient client) {
        return getPlayerConfigDir(client).resolve("key.dat");
    }

    public static void loadKeyAndPassword(MinecraftClient client) {
        if (client.getSession() == null) return;

        try {
            serverPasswords.clear();

            Path configDir = getPlayerConfigDir(client);
            Files.createDirectories(configDir);

            Path keyFile = getKeyFile(client);
            if (!Files.exists(keyFile)) {
                aesKey = KeyGenerator.getInstance("AES").generateKey();
                Files.write(keyFile, aesKey.getEncoded());
                LOGGER.info("已为玩家 {} 生成独立加密密钥", client.getSession().getUsername());
            } else {
                byte[] keyBytes = Files.readAllBytes(keyFile);
                aesKey = new SecretKeySpec(keyBytes, "AES");
            }

            Path pwdFile = getPasswordFile(client);
            if (Files.exists(pwdFile)) {
                String json = Files.readString(pwdFile);
                Map<String, String> map = new Gson().fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
                if (map != null) serverPasswords.putAll(map);
            }
        } catch (Exception e) {
            LOGGER.error("加载玩家配置失败", e);
        }
    }

    public static void savePasswords(MinecraftClient client) {
        if (client.getSession() == null) return;

        try {
            Path configDir = getPlayerConfigDir(client);
            Files.createDirectories(configDir);
            Files.writeString(getPasswordFile(client), new Gson().toJson(serverPasswords));
            LOGGER.info("已为玩家 {} 保存加密密码", client.getSession().getUsername());
        } catch (Exception e) {
            LOGGER.error("保存密码失败", e);
        }
    }

    public static void setAndSaveDefaultPassword(String password, MinecraftClient client) throws Exception {
        if (client.getSession() == null) {
            throw new IllegalStateException("Client session is null, cannot save password.");
        }

        if (aesKey == null) {
            loadKeyAndPassword(client);
        }

        String encryptedPwd = encrypt(password);
        serverPasswords.put("default", encryptedPwd);
        savePasswords(client);
        LOGGER.info("已为玩家 {} 设置并保存加密密码。", client.getSession().getUsername());
    }

    public static String encrypt(String plain) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes("UTF-8")));
    }

    public static String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty() || aesKey == null) return "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), "UTF-8");
        } catch (Exception e) {
            LOGGER.warn("密码解密失败,密钥可能已更改或文件损坏", e);
            return "";
        }
    }
}