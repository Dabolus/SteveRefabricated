package com.steve.ai.refabricated.event;

import com.steve.ai.refabricated.SteveMod;
import com.steve.ai.refabricated.entity.SteveEntity;
import com.steve.ai.refabricated.entity.SteveManager;
import com.steve.ai.refabricated.memory.StructureRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class ServerEventHandler {
    private static boolean stevesSpawned = false;

    private ServerEventHandler() {
    }

    public static void onPlayerJoin(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        SteveManager manager = SteveMod.getSteveManager();

        if (stevesSpawned) {
            return;
        }

        manager.clearAllSteves();
        StructureRegistry.clear();

        for (var entity : level.getAllEntities()) {
            if (entity instanceof SteveEntity) {
                entity.discard();
            }
        }

        Vec3 playerPos = player.position();
        Vec3 lookVec = player.getLookAngle();
        String[] names = {"Steve", "Alex", "Bob", "Charlie"};

        for (int i = 0; i < names.length; i++) {
            double offsetX = lookVec.x * 5 + (lookVec.z * (i - 1.5) * 2);
            double offsetZ = lookVec.z * 5 + (-lookVec.x * (i - 1.5) * 2);

            Vec3 spawnPos = new Vec3(
                playerPos.x + offsetX,
                playerPos.y,
                playerPos.z + offsetZ
            );

            manager.spawnSteve(level, spawnPos, names[i]);
        }

        stevesSpawned = true;
    }

    public static void onPlayerQuit() {
        stevesSpawned = false;
    }
}

