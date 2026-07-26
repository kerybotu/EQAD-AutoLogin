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
        int startY = this.height / 2 - 100;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§e修改密码"), button -> {
                    this.client.setScreen(new PasswordScreen(this));
                })
                .dimensions(centerX - 100, startY, 200, 20)
                .build());

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

        String autoJoinSubServerText = config.autoJoinSubServerEnabled ? "§a自动进入区服: 开启" : "§c自动进入区服: 关闭";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(autoJoinSubServerText), button -> {
                    config.autoJoinSubServerEnabled = !config.autoJoinSubServerEnabled;
                    config.save();
                    this.clearAndInit();
                    if (this.client != null) {
                        this.client.inGameHud.getChatHud().addMessage(
                                Text.literal("§a" + EQADAutoLogin.CHAT_PREFIX + " 自动进入区服已" + (config.autoJoinSubServerEnabled ? "开启" : "关闭"))
                        );
                    }
                })
                .dimensions(centerX - 100, startY + 90, 200, 20)
                .build());

        if (config.autoJoinSubServerEnabled) {
            String subServerText = "§b目标区服: §f" + config.getSubServerDisplayName();
            this.addDrawableChild(ButtonWidget.builder(Text.literal(subServerText), button -> {
                        config.cycleSubServer();
                        config.save();
                        this.clearAndInit();
                        if (this.client != null) {
                            this.client.inGameHud.getChatHud().addMessage(
                                    Text.literal("§a" + EQADAutoLogin.CHAT_PREFIX + " 目标区服已切换为: §f" + config.getSubServerDisplayName())
                            );
                        }
                    })
                    .dimensions(centerX - 100, startY + 120, 200, 20)
                    .build());
        }

        int advancedButtonY = config.autoJoinSubServerEnabled ? startY + 150 : startY + 120;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§d高级设置 >>"), button -> {
                    this.client.setScreen(new AdvancedConfigScreen(this));
                })
                .dimensions(centerX - 100, advancedButtonY, 200, 20)
                .build());

        int returnButtonY = config.autoJoinSubServerEnabled ? startY + 180 : startY + 150;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§f返回"), button -> this.close())
                .dimensions(centerX - 100, returnButtonY, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null) return;

        // 先让父类画完背景和所有按钮，自定义文本放最后画，保证显示在最上层，不被背景遮罩盖住
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§6§lEQAD AutoLogin 配置"), centerX, 40, 0xFFFF00);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7点击按钮切换功能状态"), centerX, 60, 0xAAAAAA);

        if (config.autoOpenMenuEnabled && config.autoJoinSubServerEnabled) {
            int warningY = this.height / 2 + 90;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§c⚠ 警告 ⚠"), centerX, warningY, 0xFF5555);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§e不建议同时开启'自动打开服务器菜单'"), centerX, warningY + 15, 0xFFAA00);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§e和'自动进入区服'功能"), centerX, warningY + 27, 0xFFAA00);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7否则会导致进入子服后重复弹出菜单"), centerX, warningY + 39, 0xAAAAAA);
        }
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