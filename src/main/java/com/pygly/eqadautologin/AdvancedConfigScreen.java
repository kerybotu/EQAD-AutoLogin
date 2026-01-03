// src/main/java/com/pygly/eqadautologin/AdvancedConfigScreen.java
package com.pygly.eqadautologin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AdvancedConfigScreen extends Screen {
    private final Screen parent;
    private ModConfig config;
    private TextFieldWidget loginDelayField;
    private TextFieldWidget menuDelayField;
    private TextFieldWidget subServerDelayField;

    public AdvancedConfigScreen(Screen parent) {
        super(Text.literal("§d§l高级设置"));
        this.parent = parent;
        this.config = EQADAutoLogin.getConfig();
    }

    @Override
    protected void init() {
        if (this.client == null || this.textRenderer == null) return;
        this.clearChildren();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 70;

        // 登录延迟输入框
        loginDelayField = new TextFieldWidget(
                this.textRenderer,
                centerX + 10,
                startY,
                100,
                20,
                Text.literal("登录延迟")
        );
        loginDelayField.setMaxLength(5);
        loginDelayField.setText(String.valueOf(config.loginDelayTicks));
        loginDelayField.setChangedListener(text -> {
            try {
                int value = Integer.parseInt(text);
                if (value >= 0 && value <= 9999) {
                    config.loginDelayTicks = value;
                    config.save();
                }
            } catch (NumberFormatException ignored) {
            }
        });
        this.addSelectableChild(loginDelayField);
        this.addDrawableChild(loginDelayField);

        // 菜单延迟输入框
        menuDelayField = new TextFieldWidget(
                this.textRenderer,
                centerX + 10,
                startY + 40,
                100,
                20,
                Text.literal("菜单延迟")
        );
        menuDelayField.setMaxLength(5);
        menuDelayField.setText(String.valueOf(config.openMenuDelayTicks));
        menuDelayField.setChangedListener(text -> {
            try {
                int value = Integer.parseInt(text);
                if (value >= 0 && value <= 9999) {
                    config.openMenuDelayTicks = value;
                    config.save();
                }
            } catch (NumberFormatException ignored) {
            }
        });
        this.addSelectableChild(menuDelayField);
        this.addDrawableChild(menuDelayField);

        // 跨服延迟输入框
        subServerDelayField = new TextFieldWidget(
                this.textRenderer,
                centerX + 10,
                startY + 80,
                100,
                20,
                Text.literal("跨服延迟")
        );
        subServerDelayField.setMaxLength(5);
        subServerDelayField.setText(String.valueOf(config.subServerDelayTicks));
        subServerDelayField.setChangedListener(text -> {
            try {
                int value = Integer.parseInt(text);
                if (value >= 0 && value <= 9999) {
                    config.subServerDelayTicks = value;
                    config.save();
                }
            } catch (NumberFormatException ignored) {
            }
        });
        this.addSelectableChild(subServerDelayField);
        this.addDrawableChild(subServerDelayField);

        // 恢复默认设置按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§6恢复默认设置"), button -> {
                    config.resetToDefault();
                    config.save();
                    loginDelayField.setText(String.valueOf(config.loginDelayTicks));
                    menuDelayField.setText(String.valueOf(config.openMenuDelayTicks));
                    subServerDelayField.setText(String.valueOf(config.subServerDelayTicks));
                    if (this.client != null) {
                        this.client.inGameHud.getChatHud().addMessage(
                                Text.literal("§a" + EQADAutoLogin.CHAT_PREFIX + " 延迟配置已恢复默认值")
                        );
                    }
                })
                .dimensions(centerX - 100, startY + 120, 200, 20)
                .build());

        // 返回按钮
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§f返回"), button -> this.close())
                .dimensions(centerX - 100, startY + 150, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null) return;
        this.renderBackground(context);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 70;

        context.drawCenteredTextWithShadow(this.textRenderer, "§d§l高级设置", centerX, 40, 0xFF55FF);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7自定义各项功能的延迟时间", centerX, 60, 0xAAAAAA);

        // 标签：登录延迟 - 左对齐显示在输入框左侧
        context.drawTextWithShadow(
                this.textRenderer,
                "§e登录延迟(ticks):",
                centerX - 110,
                startY + 6,
                0xFFFF55
        );

        // 标签：菜单延迟 - 左对齐显示在输入框左侧
        context.drawTextWithShadow(
                this.textRenderer,
                "§e菜单延迟(ticks):",
                centerX - 110,
                startY + 46,
                0xFFFF55
        );

        // 标签：跨服延迟 - 左对齐显示在输入框左侧
        context.drawTextWithShadow(
                this.textRenderer,
                "§e跨服延迟(ticks):",
                centerX - 110,
                startY + 86,
                0xFFFF55
        );

        // 提示信息
        context.drawCenteredTextWithShadow(this.textRenderer, "§71 tick = 0.05秒, 20 ticks = 1秒", centerX, this.height - 60, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7建议范围: 10-200 ticks (0.5-10秒)", centerX, this.height - 45, 0xAAAAAA);

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