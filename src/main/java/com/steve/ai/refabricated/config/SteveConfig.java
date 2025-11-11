package com.steve.ai.refabricated.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.steve.ai.refabricated.SteveMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SteveConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "steve.json";
    private static ConfigData data = new ConfigData();

    private SteveConfig() {
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                if (loaded != null) {
                    data = loaded;
                }
            } catch (IOException | JsonSyntaxException e) {
                SteveMod.LOGGER.error("Failed to load Steve config, using defaults", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            SteveMod.LOGGER.error("Failed to save Steve config", e);
        }
    }

    public static ConfigData get() {
        return data;
    }

    public static final class ConfigData {
        public String provider = "groq";
        public String apiKey = "";
        public String model = "gpt-4o-mini";
        public int maxTokens = 8000;
        public double temperature = 0.7;
        public int actionTickDelay = 20;
        public boolean enableChatResponses = true;
        public int maxActiveSteves = 10;
    }
}

