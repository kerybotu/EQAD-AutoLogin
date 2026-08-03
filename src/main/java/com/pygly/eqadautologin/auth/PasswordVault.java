package com.pygly.eqadautologin.auth;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pygly.eqadautologin.EQADConstants;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 按玩家账号独立保存的 AES 加密密码仓库。
 * 每个玩家在 ~/.eqad_autologin/&lt;用户名&gt;/ 下有自己的 key.dat + password.json，
 * 密钥不跨账号复用，一个账号的密钥泄露不会影响其他账号。
 * <p>
 * 这是从原来的 EQADAutoLogin 大类里拆出来的一块——之前加解密、文件读写、
 * 事件注册全糅在一个静态类里，现在单独成一个只管"密码怎么存"的类。
 */
public final class PasswordVault {
    private static final Logger LOGGER = LoggerFactory.getLogger(EQADConstants.MOD_ID + "-vault");
    private static final Path MOD_ROOT_DIR = Paths.get(System.getProperty("user.home"), "." + EQADConstants.MOD_ID);
    private static final String DEFAULT_KEY = "default";

    private static final PasswordVault INSTANCE = new PasswordVault();

    private final Map<String, String> encryptedPasswords = new HashMap<>();
    private SecretKey aesKey;

    private PasswordVault() {
    }

    public static PasswordVault getInstance() {
        return INSTANCE;
    }

    public boolean hasDefaultPassword() {
        return encryptedPasswords.containsKey(DEFAULT_KEY);
    }

    /** 返回解密后的默认密码；没有设置过或解密失败时返回空字符串。 */
    public String getDecryptedDefaultPassword() {
        return decrypt(encryptedPasswords.getOrDefault(DEFAULT_KEY, ""));
    }

    public void setAndSaveDefaultPassword(String password, MinecraftClient client) throws Exception {
        if (client.getSession() == null) {
            throw new IllegalStateException("Client session is null, cannot save password.");
        }
        if (aesKey == null) {
            load(client);
        }
        encryptedPasswords.put(DEFAULT_KEY, encrypt(password));
        save(client);
        LOGGER.info("已为玩家 {} 设置并保存加密密码。", client.getSession().getUsername());
    }

    /** 每次加入服务器 / 首次 tick 时调用，加载当前登录账号对应的密钥和密码。 */
    public void load(MinecraftClient client) {
        if (client.getSession() == null) return;

        try {
            encryptedPasswords.clear();

            Path configDir = playerDir(client);
            Files.createDirectories(configDir);

            Path keyFile = configDir.resolve("key.dat");
            if (!Files.exists(keyFile)) {
                aesKey = KeyGenerator.getInstance("AES").generateKey();
                Files.write(keyFile, aesKey.getEncoded());
                LOGGER.info("已为玩家 {} 生成独立加密密钥", client.getSession().getUsername());
            } else {
                aesKey = new SecretKeySpec(Files.readAllBytes(keyFile), "AES");
            }

            Path pwdFile = configDir.resolve("password.json");
            if (Files.exists(pwdFile)) {
                String json = Files.readString(pwdFile);
                Map<String, String> map = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
                }.getType());
                if (map != null) encryptedPasswords.putAll(map);
            }
        } catch (Exception e) {
            LOGGER.error("加载玩家密码失败", e);
        }
    }

    private void save(MinecraftClient client) {
        if (client.getSession() == null) return;
        try {
            Path configDir = playerDir(client);
            Files.createDirectories(configDir);
            Files.writeString(configDir.resolve("password.json"), new Gson().toJson(encryptedPasswords));
            LOGGER.info("已为玩家 {} 保存加密密码", client.getSession().getUsername());
        } catch (Exception e) {
            LOGGER.error("保存密码失败", e);
        }
    }

    private Path playerDir(MinecraftClient client) {
        String playerName = client.getSession() != null ? client.getSession().getUsername() : "unknown";
        return MOD_ROOT_DIR.resolve(playerName);
    }

    private String encrypt(String plain) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }

    private String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty() || aesKey == null) return "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.warn("密码解密失败，密钥可能已更改或文件损坏", e);
            return "";
        }
    }
}
