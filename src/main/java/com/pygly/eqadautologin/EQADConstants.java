package com.pygly.eqadautologin;

import java.util.Set;

/**
 * 全局常量。原来散落在 EQADAutoLogin 这个大类里，现在集中放一个地方，
 * 各模块都从这里取，避免到处写死字符串。
 */
public final class EQADConstants {
    private EQADConstants() {
    }

    public static final String MOD_ID = "eqad_autologin";
    public static final String CHAT_PREFIX = "§l[EQAD AutoLogin]";

    public static final Set<String> TARGET_SERVERS = Set.of(
            "vanilla.eqad.fun",
            "vanilla-v6.eqad.fun",
            "vanilla-1.eqad.fun",
            "vanilla-2.eqad.fun",
            "vanilla-3.eqad.fun"
    );
}
