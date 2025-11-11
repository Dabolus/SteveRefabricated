package com.steve.ai.refabricated.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;

public final class ClientEventHandler {

    private static boolean narratorDisabled = false;

    private ClientEventHandler() {
    }

    public static void onClientTick(Minecraft mc) {
        if (mc == null) {
            return;
        }

        if (!narratorDisabled && mc.options != null) {
            mc.options.narrator().set(NarratorStatus.OFF);
            mc.options.save();
            narratorDisabled = true;
        }

        if (KeyBindings.TOGGLE_GUI != null && KeyBindings.TOGGLE_GUI.consumeClick()) {
            SteveGUI.toggle();
        }

        SteveGUI.tick();
    }
}
