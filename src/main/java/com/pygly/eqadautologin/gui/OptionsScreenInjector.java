package com.pygly.eqadautologin.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 往原版选项界面（游戏选项 -&gt; OptionsScreen）里塞一个跳转配置界面的按钮。
 * <p>
 * {@code Screen#addDrawableChild} 本来是 protected 的，之前用反射按"参数是 Element、
 * 返回值是 Element"这个特征去找它——但泛型擦除后 {@code addDrawableChild} /
 * {@code addSelectableChild} / {@code addDrawable} 这三个方法的擦除签名完全一样，
 * 反射靠特征匹配分不清，可能选到 addSelectableChild（按钮只会被加进焦点列表，
 * 不会被加进绘制列表），导致按钮"加了但画不出来"且不报任何错。
 * <p>
 * 现在改用 Access Widener（见 {@code eqad_autologin.accesswidener}）在编译期直接把
 * addDrawableChild 开放成可调用，按方法名精确指定，不存在歧义，也不用反射了。
 */
public final class OptionsScreenInjector {
    private static final Map<Screen, ButtonWidget> INJECTED_BUTTONS = new WeakHashMap<>();

    private OptionsScreenInjector() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof OptionsScreen optionsScreen)) return;

            int x = optionsScreen.width / 2 - 100;
            int y = optionsScreen.height - 55;

            ButtonWidget existing = INJECTED_BUTTONS.get(optionsScreen);
            if (existing != null) {
                // 同一个界面实例只是分辨率/缩放变化重新 init 了一次，
                // 挪动已有按钮位置即可，不要再加一个新的，避免出现重复按钮。
                existing.setPosition(x, y);
                return;
            }

            ButtonWidget configButton = ButtonWidget.builder(
                            Text.literal("§b§l自动登录选项"),
                            button -> MinecraftClient.getInstance().setScreen(new ConfigScreen(optionsScreen))
                    )
                    .dimensions(x, y, 200, 20)
                    .build();

            optionsScreen.addDrawableChild(configButton);
            INJECTED_BUTTONS.put(optionsScreen, configButton);
        });
    }
}
