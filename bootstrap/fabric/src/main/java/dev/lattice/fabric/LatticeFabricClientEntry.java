package dev.lattice.fabric;

import dev.lattice.client.LatticeClientTest;
import dev.lattice.render.SkijaRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class LatticeFabricClientEntry implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkijaRenderer.initialize();
        String minecraftVersion = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        ClientTickEvents.END_CLIENT_TICK.register(client ->
                LatticeClientTest.openOnce(minecraftVersion));
    }
}
