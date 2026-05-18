package com.moulberry.flashback.compat;

import com.moulberry.flashback.Flashback;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

public class ExportRenderCompat {
    private static boolean checkedVisor = false;
    private static Method visorPreTickRemote = null;
    private static Method visorPostTickRemote = null;
    private static Method visorPreRenderRemote = null;

    public static void beforeClientTick() {
        invokeVisorPreTickRemote();
    }

    public static void afterClientTick() {
        invokeVisorPostTickRemote();
    }

    public static void beforeRender(float partialTick) {
        invokeVisorPreRenderRemote(partialTick);
    }

    private static boolean ensureVisorMethods() {
        if (!FabricLoader.getInstance().isModLoaded("visor")) {
            return false;
        }

        if (!checkedVisor) {
            checkedVisor = true;
            try {
                Class<?> visorClientPlayers = Class.forName("org.vmstudio.visor.core.client.player.VRClientPlayers");
                visorPreTickRemote = getVisorMethod(visorClientPlayers, "preTickRemote");
                visorPostTickRemote = getVisorMethod(visorClientPlayers, "postTickRemote");
                visorPreRenderRemote = getVisorMethod(visorClientPlayers, "preRenderRemote", float.class);
            } catch (Throwable t) {
                Flashback.LOGGER.debug("Failed to initialize Visor export render compat", t);
            }
        }

        return true;
    }

    private static Method getVisorMethod(Class<?> visorClientPlayers, String name, Class<?>... parameterTypes) {
        try {
            return visorClientPlayers.getMethod(name, parameterTypes);
        } catch (Throwable t) {
            Flashback.LOGGER.debug("Failed to initialize Visor export {} hook", name, t);
            return null;
        }
    }

    private static void invokeVisorPreTickRemote() {
        if (!ensureVisorMethods() || visorPreTickRemote == null) {
            return;
        }

        try {
            visorPreTickRemote.invoke(null);
        } catch (Throwable t) {
            Flashback.LOGGER.debug("Failed to run Visor export pre-tick hook", t);
            visorPreTickRemote = null;
        }
    }

    private static void invokeVisorPostTickRemote() {
        if (!ensureVisorMethods() || visorPostTickRemote == null) {
            return;
        }

        try {
            visorPostTickRemote.invoke(null);
        } catch (Throwable t) {
            Flashback.LOGGER.debug("Failed to run Visor export post-tick hook", t);
            visorPostTickRemote = null;
        }
    }

    private static void invokeVisorPreRenderRemote(float partialTick) {
        if (!ensureVisorMethods() || visorPreRenderRemote == null) {
            return;
        }

        try {
            visorPreRenderRemote.invoke(null, partialTick);
        } catch (Throwable t) {
            Flashback.LOGGER.debug("Failed to run Visor export pre-render hook", t);
            visorPreRenderRemote = null;
        }
    }
}