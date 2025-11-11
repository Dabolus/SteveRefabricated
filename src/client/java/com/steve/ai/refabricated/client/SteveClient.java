package com.steve.ai.refabricated.client;

import com.steve.ai.refabricated.SteveMod;
import com.steve.ai.refabricated.entity.SteveEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SteveClient implements ClientModInitializer {
    private static final ResourceLocation STEVE_TEXTURE = ResourceLocation.parse("minecraft:textures/entity/player/wide/steve.png");

    @Override
    public void onInitializeClient() {
        KeyBindings.init();

        HudRenderCallback.EVENT.register(SteveClient::renderHud);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEventHandler::onClientTick);

        EntityRendererRegistry.register(SteveMod.STEVE_ENTITY, context ->
            new HumanoidMobRenderer<SteveEntity, PlayerModel<SteveEntity>>(
                context,
                new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false),
                0.5F
            ) {
                @Override
                public ResourceLocation getTextureLocation(SteveEntity entity) {
                    return STEVE_TEXTURE;
                }
            }
        );
    }

    private static void renderHud(GuiGraphics graphics, DeltaTracker deltaTracker) {
        SteveGUI.render(graphics, deltaTracker.getGameTimeDeltaPartialTick(true));
    }
}
