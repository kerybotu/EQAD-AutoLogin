package com.pygly.eqadautologin.mixin;

import com.pygly.eqadautologin.ConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    private static final Identifier ICON_TEXTURE = new Identifier("eqad-autologin", "icon.png");

    protected MultiplayerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addConfigButton(CallbackInfo ci) {
        if (this.client == null) return;

        int x = this.width - 25;
        int y = 5;

        TexturedButtonWidget configButton = new TexturedButtonWidget(
                x, y, 20, 20,
                0, 0, 20,
                ICON_TEXTURE,
                20, 40,
                (button) -> this.client.setScreen(new ConfigScreen((MultiplayerScreen)(Object)this)),
                Text.literal("EQAD AutoLogin 配置")
        );

        this.addDrawableChild(configButton);
    }
}