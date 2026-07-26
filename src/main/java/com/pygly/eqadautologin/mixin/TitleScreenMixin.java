package com.pygly.eqadautologin.mixin;

import com.pygly.eqadautologin.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    private static final Identifier ICON_TEXTURE = Identifier.of("eqad-autologin", "textures/icon.png");

    @Shadow
    protected MinecraftClient client;

    @Shadow
    public int width;

    @Shadow
    protected abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T child);

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (this.client == null) return;

        int x = this.width - 25;
        int y = 5;

        TextIconButtonWidget button = new TextIconButtonWidget.Builder(
                Text.literal("EQAD AutoLogin 配置"),
                btn -> this.client.setScreen(new ConfigScreen((TitleScreen) (Object) this)),
                true
        )
                .texture(ICON_TEXTURE, 20, 20)
                .dimension(20, 20)
                .build();
        button.setPosition(x, y);

        this.addDrawableChild(button);
    }
}