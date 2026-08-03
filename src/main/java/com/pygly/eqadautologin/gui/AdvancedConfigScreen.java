package com.pygly.eqadautologin.gui;

import com.pygly.eqadautologin.EQADConstants;
import com.pygly.eqadautologin.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AdvancedConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    private TextFieldWidget loginDelayField;
    private TextFieldWidget menuDelayField;
    private TextFieldWidget subServerDelayField;

    public AdvancedConfigScreen(Screen parent) {
        super(Text.literal("§d§l高级设置"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        if (this.client == null || this.textRenderer == null) return;
        this.clearChildren();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 70;

        loginDelayField = new TextFieldWidget(
                this.textRenderer, centerX + 10, startY, 100, 20, Text.literal("登录延迟")
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

        menuDelayField = new TextFieldWidget(
                this.textRenderer, centerX + 10, startY + 40, 100, 20, Text.literal("菜单延迟")
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

        subServerDelayField = new TextFieldWidget(
                this.textRenderer, centerX + 10, startY + 80, 100, 20, Text.literal("跨服延迟")
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

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§6恢复默认设置"), button -> {
                    config.resetToDefault();
                    config.save();
                    loginDelayField.setText(String.valueOf(config.loginDelayTicks));
                    menuDelayField.setText(String.valueOf(config.openMenuDelayTicks));
                    subServerDelayField.setText(String.valueOf(config.subServerDelayTicks));
                    if (this.client != null) {
                        this.client.inGameHud.getChatHud().addMessage(
                                Text.literal("§a" + EQADConstants.CHAT_PREFIX + " 延迟配置已恢复默认值")
                        );
                    }
                })
                .dimensions(centerX - 100, startY + 120, 200, 20)
                .build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§f返回"), button -> this.close())
                .dimensions(centerX - 100, startY + 150, 200, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null) return;

        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 70;

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§d§l高级设置"), centerX, 40, 0xFFFF55FF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7自定义各项功能的延迟时间"), centerX, 60, 0xFFAAAAAA);

        context.drawTextWithShadow(this.textRenderer, Text.literal("§e登录延迟(ticks):"), centerX - 110, startY + 6, 0xFFFFFF55);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§e菜单延迟(ticks):"), centerX - 110, startY + 46, 0xFFFFFF55);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§e跨服延迟(ticks):"), centerX - 110, startY + 86, 0xFFFFFF55);

        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§71 tick = 0.05秒, 20 ticks = 1秒"), centerX, this.height - 60, 0xFFAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7建议范围: 10-200 ticks (0.5-10秒)"), centerX, this.height - 45, 0xFFAAAAAA);
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
