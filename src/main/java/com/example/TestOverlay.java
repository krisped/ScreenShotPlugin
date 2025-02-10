package com.example;

import java.awt.*;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.*;

public class TestOverlay extends Overlay {

    private final Client client;
    private final TestPlugin plugin;
    private final TestConfig config;

    @Inject
    public TestOverlay(Client client, TestPlugin plugin, TestConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showSmiteDrain()) {
            return null; // Ikke vis noe hvis det er slått av i konfigurasjonen
        }

        int smitedPrayer = plugin.getSmitedPrayer();
        if (smitedPrayer <= 0) {
            return null; // Ikke vis overlayet hvis ingen prayer er smitet
        }

        String text = "Smited: " + smitedPrayer;
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 14));
        graphics.drawString(text, 10, 20);

        return new Dimension(100, 20);
    }
}
