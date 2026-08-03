package com.pygly.eqadautologin;

import com.pygly.eqadautologin.feature.AutoLoginController;
import com.pygly.eqadautologin.gui.OptionsScreenInjector;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 入口类：只负责把各个模块组装起来，不再包含任何具体业务逻辑。
 * 具体功能分别在 feature.AutoLoginController（自动登录/跨服/开菜单）
 * 和 gui.OptionsScreenInjector（选项界面按钮注入）里。
 */
public class EQADAutoLoginClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EQADConstants.MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("EQAD AutoLogin Mod is starting...");
        AutoLoginController.getInstance().registerEvents();
        OptionsScreenInjector.register();
    }
}
