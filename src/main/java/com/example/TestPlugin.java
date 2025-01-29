package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@PluginDescriptor(
        name = "[KP] TEST",
        description = "Viser widget spawn ID, debug-modus, skjermbilde og sender til Discord",
        tags = {"widget", "test", "overlay", "discord", "screenshot"}
)
public class TestPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private TestConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private TestOverlay testOverlay;

    private int lastWidgetId = -1;
    private int screenshotDelayTicks = 0; // Antall ticks til skjermbilde tas
    private int delayedWidgetId = -1; // Widget ID som venter på skjermbilde

    @Provides
    TestConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(TestConfig.class);
    }

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(testOverlay);
        log.info("[KP] TEST Plugin started!");
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(testOverlay);
        log.info("[KP] TEST Plugin stopped!");
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        int widgetId = event.getGroupId();
        lastWidgetId = widgetId;

        if (config.debugWidgetSpawns())
        {
            String debugMessage = "Widget spawned: " + widgetId;
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", debugMessage, null);
            testOverlay.addMessage(debugMessage);
        }

        // Sjekk om widget-ID samsvarer med screenshot-widget-ID
        if (config.screenshotWidget() && widgetId == config.screenshotWidgetId())
        {
            delayedWidgetId = widgetId; // Lagre widget-ID
            screenshotDelayTicks = 2; // Sett en forsinkelse på 2 ticks
        }

        log.info("Detected Widget: " + widgetId);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (screenshotDelayTicks > 0)
        {
            screenshotDelayTicks--;
            if (screenshotDelayTicks == 0 && delayedWidgetId != -1)
            {
                takeScreenshotAndSendToDiscord(delayedWidgetId);
                delayedWidgetId = -1; // Tilbakestill forsinkelsen
            }
        }
    }

    private void takeScreenshotAndSendToDiscord(int widgetId)
    {
        try
        {
            BufferedImage screenshot = takeScreenshot();
            if (screenshot != null)
            {
                String webhookUrl = config.webhookUrl();
                if (webhookUrl != null && !webhookUrl.isEmpty())
                {
                    sendScreenshotToDiscord(webhookUrl, screenshot, "Widget ID: " + widgetId + " Screenshot");
                }
            }
        }
        catch (Exception e)
        {
            log.error("Failed to capture and send screenshot", e);
        }
    }

    private BufferedImage takeScreenshot()
    {
        try
        {
            // Finn klientens posisjon på skjermen
            Point location = client.getCanvas().getLocationOnScreen();
            int width = client.getCanvas().getWidth();
            int height = client.getCanvas().getHeight();

            // Bruk Robot til å fange innholdet
            Robot robot = new Robot();
            Rectangle captureArea = new Rectangle(location.x, location.y, width, height);
            return robot.createScreenCapture(captureArea);
        }
        catch (Exception e)
        {
            log.error("Failed to capture screenshot", e);
            return null;
        }
    }

    private void sendScreenshotToDiscord(String webhookUrl, BufferedImage image, String message)
    {
        try
        {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----Boundary");

            String boundary = "----Boundary";
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", imageStream);
            byte[] imageBytes = imageStream.toByteArray();

            StringBuilder payload = new StringBuilder();
            payload.append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"content\"\r\n\r\n")
                    .append(message).append("\r\n")
                    .append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"file\"; filename=\"screenshot.png\"\r\n")
                    .append("Content-Type: image/png\r\n\r\n");

            try (OutputStream outputStream = connection.getOutputStream())
            {
                outputStream.write(payload.toString().getBytes());
                outputStream.write(imageBytes);
                outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
            }

            connection.getResponseCode(); // Trigger request
        }
        catch (Exception e)
        {
            log.error("Failed to send screenshot to Discord", e);
        }
    }

    public int getLastWidgetId()
    {
        return lastWidgetId;
    }
}
