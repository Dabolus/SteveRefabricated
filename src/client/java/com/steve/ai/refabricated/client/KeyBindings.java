package com.steve.ai.refabricated.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {

    public static final String KEY_CATEGORY = "key.categories.steve";

    public static KeyMapping TOGGLE_GUI;

    private KeyBindings() {
    }

    public static void init() {
        TOGGLE_GUI = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.steve.toggle_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            KEY_CATEGORY
        ));
    }
}
