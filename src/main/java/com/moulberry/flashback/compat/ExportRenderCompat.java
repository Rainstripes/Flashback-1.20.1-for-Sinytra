package com.moulberry.flashback.compat;

import com.moulberry.flashback.Flashback;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

public class ExportRenderCompat {
    private static boolean checkedVisor = false;
    private static Method visorPreRenderRemote = null;

    public static void beforeRender(float partialTick) {
        invokeVisorPreRenderRemote(partialTick);
    }

    private static void invokeVisorPreRenderRemote(float partialTick) {
        if (!FabricLoader.getInstance().isModLoaded("visor")) {
            return;
        }

        if (!checkedVisor) {
            checkedVisor = true;
            try {
                Class<?> visorClientPlayers = Class.forName("org.vmstudio.visor.core.client.player.VRClientPlayers");
                visorPreRenderRemote = visorClientPlayers.getMethod("preRenderRemote", float.class);
            } catch (Throwable t) {
                Flashback.LOGGER.debug("Failed to initialize Visor export render compat", t);
            }
        }

        if (visorPreRenderRemote == null) {
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