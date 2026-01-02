package com.pygly.eqadautologin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private ModConfig config;

    public ConfigScreen(Screen parent) {
        super(Text.literal("§6§lEQAD AutoLogin 配置"));
        this.parent = parent;
        this.config = EQADAutoLogin.getConfig();
    }

    @Override
    protected void init() {
        if (this.client == null) return;
        this.clearChildren();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        // 按钮 A: 修改密码
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§e修改密码"), button -> {
                    this.client.setScreen(new PasswordScreen(this));
                })
                .dimensions(centerX - 100, startY, 200, 20)
                .build());

        // 按钮 B: 自动登录开关
        String autoLoginText = config.autoLoginEnabled ? "§a自动登录: 开启" : "§c自动登录: 关闭";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(autoLoginText), button -> {
                    config.autoLoginEnabled = !config.autoLoginEnabled;
                    config.save();
                    this.clearAndInit();
                    if (this.client != null) {
                        this.client.inGameHud.getChatHud().addMessage(
                                Text.literal("§a" + EQADAutoLogin.CHAT_PREFIX + " 自动登录已" + (config.autoLoginEnabled ? "开启" : "关闭"))
                        );
                    }
                })
                .dimensions(centerX - 100, startY + 30, 200, 20)
                .build());

        // 按钮 C: 自动打开服务器菜单开关
        String autoMenuText = config.autoOpenMenuEnabled ? "§a自动打开服务器菜单: 开启" : "§c自动打开服务器菜单: 关闭";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(autoMenuText), button -> {
                    config.autoOpenMenuEnabled = !config.autoOpenMenuEnabled;
                    config.save();
                    this.clearAndInit();
                    if (this.client != null) {
                        this.client.inGameHud.getChatHud().addMessage(
                                Text.literal("§a" + EQADAutoLogin.CHAT_PREFIX + " 自动打开菜单已" + (config.autoOpenMenuEnabled ? "开启" : "关闭"))
                        );
                    }
                })
                .dimensions(centerX - 100, startY + 60, 200, 20)
                .build());

        // 返回按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§f返回"), button -> this.close())
                .dimensions(centerX - 100, startY + 100, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null) return;
        this.renderBackground(context);

        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, "§6§lEQAD AutoLogin 配置", centerX, 40, 0xFFFF00);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7点击按钮切换功能状态", centerX, 60, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}