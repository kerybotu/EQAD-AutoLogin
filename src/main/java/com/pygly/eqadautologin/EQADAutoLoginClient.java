package com.pygly.eqadautologin;

import net.fabricmc.api.ClientModInitializer;

public class EQADAutoLoginClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EQADAutoLogin.LOGGER.info("EQAD AutoLogin Mod is starting...");
        EQADAutoLogin.registerEvents();
    }
}