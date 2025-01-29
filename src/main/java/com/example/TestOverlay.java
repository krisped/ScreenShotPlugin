package com.example;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TestOverlay extends Overlay
{
    private final Client client;
    private final TestPlugin plugin;
    private final Deque<String> widgetMessages = new ArrayDeque<>();

    @Inject
    public TestOverlay(Client client, TestPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    public void addMessage(String message)
    {
        widgetMessages.add(message);
        if (widgetMessages.size() > 5) // Beholder kun de 5 siste meldingene
        {
            widgetMessages.poll();
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        // Tegn boks og meldinger
        int x = 10, y = 40;
        graphics.setColor(new Color(0, 0, 0, 128)); // Bakgrunnsfarge for boksen (semi-transparent svart)
        graphics.fillRect(x - 5, y - 20, 200, widgetMessages.size() * 20 + 10);

        graphics.setColor(Color.WHITE); // Tekstfarge
        int lineY = y;
        for (String message : widgetMessages)
        {
            graphics.drawString(message, x, lineY);
            lineY += 20;
        }

        return null;
    }
}
