package dev.lattice.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-neutral bootstrap code. Nothing in this package may reference a
 * Minecraft or mod-loader class: the same source is compiled into all four
 * loader/version artifacts.
 */
public final class Lattice {
    public static final String MOD_ID = "lattice";
    public static final String MOD_NAME = "Lattice";
    public static final String VERSION = "0.1.0";

    private static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
    private static boolean initialized;

    private Lattice() {
    }

    public static synchronized void initialize(String loader, String minecraftVersion, String adapterId) {
        if (initialized) {
            LOG.debug("[{}] initialize() called again; ignoring duplicate bootstrap", MOD_ID);
            return;
        }
        initialized = true;

        LOG.info("[{}] Lattice {} initialized", MOD_ID, VERSION);
        LOG.info("[{}] loader={}, minecraft={}, adapter={}, java={}",
                MOD_ID, loader, minecraftVersion, adapterId, System.getProperty("java.version"));
    }
}
