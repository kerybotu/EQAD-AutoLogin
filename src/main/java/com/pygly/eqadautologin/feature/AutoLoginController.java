package com.pygly.eqadautologin.feature;

import com.pygly.eqadautologin.EQADConstants;
import com.pygly.eqadautologin.auth.PasswordVault;
import com.pygly.eqadautologin.config.ModConfig;
import com.pygly.eqadautologin.gui.PasswordScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 自动登录 / 自动跨服 / 自动开菜单的核心状态机。
 * <p>
 * 旧版本里这三件事各用一组 {@code xxxDelay}/{@code needXxx} 静态字段手写倒计时，
 * 三份几乎一样的代码，还很容易漏改。这里统一成一个延迟任务队列：
 * {@link #schedule(int, Runnable)} 排一个"多少 tick 后执行什么"，
 * 每个动作自己决定要不要重新排队重试，调度逻辑本身不用再关心具体是哪个功能。
 */
public final class AutoLoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EQADConstants.MOD_ID);

    private static final AutoLoginController INSTANCE = new AutoLoginController();

    private final PasswordVault vault = PasswordVault.getInstance();
    private final Deque<PendingAction> pending = new ArrayDeque<>();

    private boolean hasAutoLoggedInThisSession = false;
    private boolean initialCheckDone = false;

    private AutoLoginController() {
    }

    public static AutoLoginController getInstance() {
        return INSTANCE;
    }

    public void registerEvents() {
        ModConfig.getInstance(); // 提前触发一次配置加载

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onJoinServer(client));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            hasAutoLoggedInThisSession = false;
            pending.clear();
            LOGGER.info("玩家已断开连接，重置自动登录状态");
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void onJoinServer(MinecraftClient client) {
        vault.load(client);
        String address = client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "";
        if (!EQADConstants.TARGET_SERVERS.contains(address) || hasAutoLoggedInThisSession) {
            return;
        }

        ModConfig config = ModConfig.getInstance();

        if (config.autoLoginEnabled) {
            double loginSeconds = config.loginDelayTicks / 20.0;
            chat(client, String.format("§e%s 检测到纯净服,%.1f秒后自动登录...", EQADConstants.CHAT_PREFIX, loginSeconds));
            schedule(config.loginDelayTicks, () -> performAutoLogin(client));

            if (config.autoJoinSubServerEnabled) {
                schedule(config.subServerDelayTicks, () -> performAutoJoinSubServer(client));
            }
            if (config.autoOpenMenuEnabled) {
                schedule(config.openMenuDelayTicks, () -> performAutoOpenMenu(client));
            }
        } else if (config.autoOpenMenuEnabled) {
            chat(client, "§e" + EQADConstants.CHAT_PREFIX + " 检测到纯净服,即将自动打开服务器菜单...");
            schedule(config.openMenuDelayTicks, () -> performAutoOpenMenu(client));
        }

        hasAutoLoggedInThisSession = true;
    }

    private void tick(MinecraftClient client) {
        if (!initialCheckDone && client.getSession() != null) {
            initialCheckDone = true;
            client.execute(() -> {
                vault.load(client);
                if (!vault.hasDefaultPassword() && client.currentScreen == null) {
                    client.setScreen(new PasswordScreen(null));
                }
            });
        }

        if (pending.isEmpty()) return;

        // 每 tick 把队列里当前所有任务各推进一格；没到时间的重新入队，
        // 到时间的直接执行。任务数量很小（最多 3 个），用不着更复杂的调度器。
        int size = pending.size();
        for (int i = 0; i < size; i++) {
            PendingAction action = pending.poll();
            if (action == null) continue;
            action.ticksRemaining--;
            if (action.ticksRemaining <= 0) {
                action.runnable.run();
            } else {
                pending.offer(action);
            }
        }
    }

    private void schedule(int delayTicks, Runnable runnable) {
        pending.offer(new PendingAction(Math.max(delayTicks, 0), runnable));
    }

    private void performAutoLogin(MinecraftClient client) {
        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            if (player == null || player.networkHandler == null) {
                schedule(ModConfig.getInstance().loginDelayTicks, () -> performAutoLogin(client));
                return;
            }

            String pwd = vault.getDecryptedDefaultPassword();
            if (pwd.isEmpty()) {
                client.setScreen(new PasswordScreen(client.currentScreen));
                return;
            }

            performLoginCommand(client, pwd);
        });
    }

    private void performAutoJoinSubServer(MinecraftClient client) {
        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            if (player == null || player.networkHandler == null) {
                schedule(10, () -> performAutoJoinSubServer(client));
                return;
            }

            ModConfig config = ModConfig.getInstance();
            try {
                player.networkHandler.sendChatCommand("server " + config.targetSubServer);
                chat(client, "§a" + EQADConstants.CHAT_PREFIX + " 正在自动进入" + config.getSubServerDisplayName() + "...");
                LOGGER.info("玩家 {} 自动跨服指令已发送: /server {}", player.getName().getString(), config.targetSubServer);
            } catch (Exception e) {
                LOGGER.error("发送跨服指令失败!", e);
                chat(client, "§c" + EQADConstants.CHAT_PREFIX + " 自动跨服失败!");
            }
        });
    }

    private void performAutoOpenMenu(MinecraftClient client) {
        client.execute(() -> {
            ClientPlayerEntity player = client.player;
            if (player == null || player.networkHandler == null) {
                schedule(ModConfig.getInstance().openMenuDelayTicks, () -> performAutoOpenMenu(client));
                return;
            }

            try {
                player.networkHandler.sendChatCommand("cd");
                chat(client, "§a" + EQADConstants.CHAT_PREFIX + " 自动打开服务器菜单指令已发送!");
                LOGGER.info("玩家 {} 自动打开菜单指令已发送", player.getName().getString());
            } catch (Exception e) {
                LOGGER.error("发送打开菜单指令失败!", e);
                chat(client, "§c" + EQADConstants.CHAT_PREFIX + " 自动打开菜单失败!");
            }
        });
    }

    /** 供 PasswordScreen 在用户手动设置完密码后立即触发一次登录调用。 */
    public void performLoginCommand(MinecraftClient client, String password) {
        ClientPlayerEntity player = client.player;
        if (player == null || player.networkHandler == null) return;

        try {
            player.networkHandler.sendChatCommand("l " + password);
            chat(client, "§a" + EQADConstants.CHAT_PREFIX + " 自动登录指令已发送!");
            LOGGER.info("玩家 {} 登录指令已发送", player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("发送登录指令失败!", e);
            chat(client, "§c" + EQADConstants.CHAT_PREFIX + " 自动登录失败!请手动输入。");
        }
    }

    private void chat(MinecraftClient client, String message) {
        client.inGameHud.getChatHud().addMessage(Text.literal(message));
    }

    private static final class PendingAction {
        int ticksRemaining;
        final Runnable runnable;

        PendingAction(int ticksRemaining, Runnable runnable) {
            this.ticksRemaining = ticksRemaining;
            this.runnable = runnable;
        }
    }
}
