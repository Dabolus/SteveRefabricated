package com.steve.ai.refabricated.structure;

import com.steve.ai.refabricated.SteveMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads Minecraft structure templates from NBT files for sequential block-by-block placement
 */
public class StructureTemplateLoader {
    
    private static final String CREATEMOD_BASE_URL = "https://createmod.com";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    
    public static class TemplateBlock {
        public final BlockPos relativePos;
        public final BlockState blockState;
        
        public TemplateBlock(BlockPos relativePos, BlockState blockState) {
            this.relativePos = relativePos;
            this.blockState = blockState;
        }
    }
    
    public static class LoadedTemplate {
        public final String name;
        public final List<TemplateBlock> blocks;
        public final int width;
        public final int height;
        public final int depth;
        
        public LoadedTemplate(String name, List<TemplateBlock> blocks, int width, int height, int depth) {
            this.name = name;
            this.blocks = blocks;
            this.width = width;
            this.height = height;
            this.depth = depth;
        }
    }
    
    /**
     * Load a structure from createmod.com
     */
    public static LoadedTemplate loadFromNBT(String structureName) {
        SteveMod.LOGGER.info("Searching for structure '{}' on createmod.com", structureName);
        LoadedTemplate fromUrl = loadFromURL(structureName);
        
        if (fromUrl == null) {
            SteveMod.LOGGER.warn("Structure '{}' not found on createmod.com", structureName);
        }
        
        return fromUrl;
    }
    
    /**
     * Load a structure from createmod.com by searching and downloading
     */
    private static LoadedTemplate loadFromURL(String searchTerm) {
        try {
            // Step 1: Search for the schematic
            String searchUrl = CREATEMOD_BASE_URL + "/search/" + searchTerm.replace(" ", "-") + "?sort=1&rating=5&mcv=1.21.X";
            SteveMod.LOGGER.info("Searching: {}", searchUrl);
            
            String searchHtml = fetchUrl(searchUrl);
            if (searchHtml == null) {
                SteveMod.LOGGER.warn("Failed to fetch search results");
                return null;
            }
            
            // Step 2: Find first schematic link
            String schematicPath = findFirstSchematicLink(searchHtml);
            if (schematicPath == null) {
                SteveMod.LOGGER.warn("No schematic found for search term: {}", searchTerm);
                return null;
            }
            
            // Step 3: Fetch the schematic page
            String schematicUrl = CREATEMOD_BASE_URL + schematicPath;
            SteveMod.LOGGER.info("Found schematic page: {}", schematicUrl);
            
            String schematicHtml = fetchUrl(schematicUrl);
            if (schematicHtml == null) {
                SteveMod.LOGGER.warn("Failed to fetch schematic page");
                return null;
            }
            
            // Step 4: Find the .nbt file download link
            String nbtFilePath = findNbtFileLink(schematicHtml);
            if (nbtFilePath == null) {
                SteveMod.LOGGER.warn("No .nbt file found on schematic page");
                return null;
            }
            
            // Step 5: Download the .nbt file
            String nbtUrl = CREATEMOD_BASE_URL + nbtFilePath;
            SteveMod.LOGGER.info("Downloading NBT file: {}", nbtUrl);
            
            byte[] nbtData = downloadFile(nbtUrl);
            if (nbtData == null) {
                SteveMod.LOGGER.warn("Failed to download .nbt file");
                return null;
            }
            
            // Step 6: Parse the NBT data
            return loadFromBytes(nbtData, searchTerm);
            
        } catch (Exception e) {
            SteveMod.LOGGER.error("Error loading structure from URL", e);
            return null;
        }
    }
    
    /**
     * Fetch HTML content from a URL
     */
    private static String fetchUrl(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
            
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                SteveMod.LOGGER.warn("HTTP request failed with status: {}", response.statusCode());
                return null;
            }
        } catch (Exception e) {
            SteveMod.LOGGER.error("Error fetching URL: {}", url, e);
            return null;
        }
    }
    
    /**
     * Find the first link to a schematic page (starts with /schematics/)
     */
    private static String findFirstSchematicLink(String html) {
        Pattern pattern = Pattern.compile("<a[^>]+href=\"(/schematics/[^\"]+)\"");
        Matcher matcher = pattern.matcher(html);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Find the first .nbt file download link (starts with /api/files)
     */
    private static String findNbtFileLink(String html) {
        Pattern pattern = Pattern.compile("<a[^>]+href=\"(/api/files/[^\"]+\\.nbt)\"");
        Matcher matcher = pattern.matcher(html);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Download binary file from a URL
     */
    private static byte[] downloadFile(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
            
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                SteveMod.LOGGER.warn("HTTP request failed with status: {}", response.statusCode());
                return null;
            }
        } catch (Exception e) {
            SteveMod.LOGGER.error("Error downloading file: {}", url, e);
            return null;
        }
    }
    
    /**
     * Load structure from byte array
     */
    private static LoadedTemplate loadFromBytes(byte[] data, String name) {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            CompoundTag nbt = NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap());
            return parseNBTStructure(nbt, name);
        } catch (IOException e) {
            SteveMod.LOGGER.error("Failed to parse NBT data", e);
            return null;
        }
    }
    

    
    /**
     * Parse a structure from raw NBT data
     */
    private static LoadedTemplate parseNBTStructure(CompoundTag nbt, String name) {
        List<TemplateBlock> blocks = new ArrayList<>();
        
        var sizeList = nbt.getList("size", 3); // 3 = TAG_Int
        int width = sizeList.getInt(0);
        int height = sizeList.getInt(1);
        int depth = sizeList.getInt(2);
        
        var paletteList = nbt.getList("palette", 10); // 10 = TAG_Compound
        List<BlockState> palette = new ArrayList<>();
        
        for (int i = 0; i < paletteList.size(); i++) {
            CompoundTag blockTag = paletteList.getCompound(i);
            String blockName = blockTag.getString("Name");
            
            try {
                ResourceLocation blockLocation = ResourceLocation.parse(blockName);
                Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(blockLocation);
                palette.add(block.defaultBlockState());
            } catch (Exception e) {
                SteveMod.LOGGER.warn("Unknown block in structure: {}", blockName);
                palette.add(Blocks.AIR.defaultBlockState());
            }
        }
        
        var blocksList = nbt.getList("blocks", 10);
        for (int i = 0; i < blocksList.size(); i++) {
            CompoundTag blockTag = blocksList.getCompound(i);
            
            int paletteIndex = blockTag.getInt("state");
            var posList = blockTag.getList("pos", 3);
            
            BlockPos pos = new BlockPos(
                posList.getInt(0),
                posList.getInt(1),
                posList.getInt(2)
            );
            
            BlockState state = palette.get(paletteIndex);
            if (!state.isAir()) {
                blocks.add(new TemplateBlock(pos, state));
            }
        }
        
        SteveMod.LOGGER.info("Loaded {} blocks from NBT '{}' ({}x{}x{})", blocks.size(), name, width, height, depth);
        return new LoadedTemplate(name, blocks, width, height, depth);
    }
    

}

