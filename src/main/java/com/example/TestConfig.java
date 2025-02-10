package com.example;

import net.runelite.client.config.*;

@ConfigGroup("testplugin")
public interface TestConfig extends Config {

    @ConfigItem(
            keyName = "showSmiteDrain",
            name = "Show Smite Drain",
            description = "Toggle to show the total smited prayer on screen."
    )
    default boolean showSmiteDrain() {
        return true;
    }

    @ConfigItem(
            keyName = "overlayDisplayDuration",
            name = "Overlay Duration",
            description = "How long the opponent info should be displayed after combat (in seconds)."
    )
    default int overlayDisplayDuration() {
        return 10;
    }
}
