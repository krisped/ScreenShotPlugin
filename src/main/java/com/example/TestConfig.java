package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("testplugin")
public interface TestConfig extends Config
{
    @ConfigItem(
            keyName = "debugWidgetSpawns",
            name = "Debug Widget Spawns",
            description = "Log all widget spawns to chat and overlay"
    )
    default boolean debugWidgetSpawns()
    {
        return false;
    }

    @ConfigItem(
            keyName = "screenshotWidget",
            name = "Screenshot Widget",
            description = "Take a screenshot when a specific widget ID spawns"
    )
    default boolean screenshotWidget()
    {
        return false;
    }

    @ConfigItem(
            keyName = "screenshotWidgetId",
            name = "Screenshot Widget ID",
            description = "The widget ID to trigger screenshot"
    )
    default int screenshotWidgetId()
    {
        return -1;
    }

    @ConfigItem(
            keyName = "webhookUrl",
            name = "Discord Webhook URL",
            description = "Set webhook URL to send widget messages and screenshots to Discord"
    )
    default String webhookUrl()
    {
        return "";
    }
}
