package com.pygly.eqadautologin.mixin;

import com.pygly.eqadautologin.ConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
    private static final Identifier ICON_TEXTURE = Identifier.of("eqad-autologin", "icon.png");

    // 这个构造方法永远不会真正被调用（Mixin 合并时会被丢弃），
    // 只是为了满足 "extends Screen" 在编译期的语法要求。
    private MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addConfigButton(CallbackInfo ci) {
        if (this.client == null) return;

        int x = this.width - 25;
        int y = 5;

        TextIconButtonWidget configButton = new TextIconButtonWidget.Builder(
                Text.literal("EQAD AutoLogin 配置"),
                (button) -> this.client.setScreen(new ConfigScreen((MultiplayerScreen)(Object)this)),
                true // hideText: 只显示图标，不显示文字
        )
                .texture(ICON_TEXTURE, 20, 20)
                .dimension(20, 20)
                .build();
        configButton.setPosition(x, y);

        this.addDrawableChild(configButton);
    }
}