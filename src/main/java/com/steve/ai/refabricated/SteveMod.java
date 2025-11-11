package com.steve.ai.refabricated;

import com.steve.ai.refabricated.command.SteveCommands;
import com.steve.ai.refabricated.config.SteveConfig;
import com.steve.ai.refabricated.entity.SteveEntity;
import com.steve.ai.refabricated.entity.SteveManager;
import com.steve.ai.refabricated.event.ServerEventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SteveMod implements ModInitializer {
    public static final String MODID = "steve";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final EntityType<SteveEntity> STEVE_ENTITY = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(MODID, "steve"),
        FabricEntityTypeBuilder.create(MobCategory.CREATURE, SteveEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
            .trackRangeChunks(10)
            .build()
    );

    private static SteveManager steveManager;

    @Override
    public void onInitialize() {
        SteveConfig.load();
        steveManager = new SteveManager();

        FabricDefaultAttributeRegistry.register(STEVE_ENTITY, SteveEntity.createAttributes());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            SteveCommands.register(dispatcher)
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            ServerEventHandler.onPlayerJoin(handler.player)
        );

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            ServerEventHandler.onPlayerQuit()
        );
    }

    public static SteveManager getSteveManager() {
        return steveManager;
    }
}

