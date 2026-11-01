package dev.lattice.fabric;

import dev.lattice.render.SkijaRenderer;
import net.fabricmc.api.ClientModInitializer;

public final class LatticeFabricClientEntry implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkijaRenderer.initialize();
    }
}
