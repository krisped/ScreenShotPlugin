package com.example;

import com.google.inject.Provides;
import java.awt.Color;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.hiscore.HiscoreEndpoint;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Test Plugin",
        description = "Viser informasjon om motstanderen i kamp",
        tags = {"combat", "health", "hitpoints", "overlay"}
)
public class TestPlugin extends Plugin {

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.0");

    @Inject
    private Client client;

    @Inject
    private TestConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private TestOverlay overlay;

    @Inject
    private EventBus eventBus;

    @Getter(AccessLevel.PACKAGE)
    private HiscoreEndpoint hiscoreEndpoint = HiscoreEndpoint.NORMAL;

    @Getter(AccessLevel.PACKAGE)
    private Instant lastTime;

    private Actor lastOpponent;
    private Hitsplat lastHitsplat;
    private int smitedPrayer = 0;

    public int getSmitedPrayer() {
        return smitedPrayer;
    }

    @Provides
    TestConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TestConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
        eventBus.register(this);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        eventBus.unregister(this);
        lastOpponent = null;
        lastTime = null;
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event) {
        if (event.getSource() != client.getLocalPlayer()) {
            return;
        }
        Actor target = event.getTarget();
        if (target != null && target != lastOpponent) {
            smitedPrayer = 0;
        }
        lastOpponent = target;
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (!client.isPrayerActive(Prayer.SMITE)) {
            return;
        }
        if (event.getActor() != lastOpponent || event.getHitsplat() == lastHitsplat) {
            return;
        }
        lastHitsplat = event.getHitsplat();

        int damage = event.getHitsplat().getAmount();
        int drain = damage / 4;
        if (drain > 0) {
            smitedPrayer += drain;
            System.out.println("Damage: " + damage + ", Drain: " + drain + ", Total Smited Prayer: " + smitedPrayer);
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (lastOpponent != null && lastTime != null && client.getLocalPlayer().getInteracting() == null) {
            if (Duration.between(lastTime, Instant.now()).toSeconds() > config.overlayDisplayDuration()) {
                lastOpponent = null;
                smitedPrayer = 0;
            }
        }
    }

    private static String getPercentText(int current, int maximum) {
        double percent = 100.0 * current / maximum;
        return PERCENT_FORMAT.format(percent) + "%";
    }
}
