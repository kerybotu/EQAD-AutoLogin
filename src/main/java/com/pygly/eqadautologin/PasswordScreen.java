package com.pygly.eqadautologin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class PasswordScreen extends Screen {
    private Screen parent;
    private TextFieldWidget passwordField;
    private String actualPassword = "";

    public PasswordScreen(Screen parent) {
        super(Text.literal("设置 EQAD 纯净服登入密码"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (this.client == null || this.textRenderer == null) return;
        this.clearChildren();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        passwordField = new TextFieldWidget(
                this.textRenderer,
                centerX - 100,
                centerY - 10,
                200,
                20,
                Text.literal("密码")
        );
        passwordField.setMaxLength(128);
        passwordField.setChangedListener(text -> actualPassword = text);
        passwordField.setRenderTextProvider((text, firstCharPos) ->
                Text.literal("●".repeat(text.length())).asOrderedText()
        );

        this.addSelectableChild(passwordField);
        this.addDrawableChild(passwordField);
        this.setInitialFocus(passwordField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("确定"), button -> {
                    if (actualPassword.isEmpty()) {
                        this.client.inGameHud.getChatHud().addMessage(Text.literal("§c" + EQADAutoLogin.CHAT_PREFIX + " 密码不能为空！"));
                        return;
                    }
                    try {
                        EQADAutoLogin.setAndSaveDefaultPassword(actualPassword, this.client);
                        this.client.inGameHud.getChatHud().addMessage(Text.literal("§a§l" + EQADAutoLogin.CHAT_PREFIX + " 密码已安全保存！"));
                        EQADAutoLogin.performLoginCommand(this.client, actualPassword);
                        this.close();
                    } catch (Exception e) {
                        EQADAutoLogin.LOGGER.error("密码保存失败", e);
                        this.client.inGameHud.getChatHud().addMessage(Text.literal("§c" + EQADAutoLogin.CHAT_PREFIX + " 保存失败"));
                    }
                })
                .dimensions(centerX - 102, centerY + 20, 100, 20)
                .build());

        String cancelText = parent == null ? "跳过" : "取消";
        this.addDrawableChild(ButtonWidget.builder(Text.literal(cancelText), button -> this.close())
                .dimensions(centerX + 2, centerY + 20, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null) return;
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§6§lEQAD 纯净服自动登录"), centerX, 35, 0xFFFF00);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§e请只输入密码（如：123456）"), centerX, 65, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7密码已加密存储于你的用户目录"), centerX, 85, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7每个账号独立保存，永不泄露"), centerX, 100, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7可在多人游戏界面随时修改"), centerX, 115, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        Screen previousParent = this.parent;
        this.parent = null;
        this.init(client, width, height);
        this.parent = previousParent;
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