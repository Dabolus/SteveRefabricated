package com.steve.ai.refabricated.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.steve.ai.refabricated.SteveMod;
import com.steve.ai.refabricated.entity.SteveEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Side-mounted GUI panel for Steve agent interaction.
 * Inspired by Cursor's composer - slides in/out from the right side.
 * Now with scrollable message history!
 */
public class SteveGUI {
    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_PADDING = 6;
    private static final int HEADER_HEIGHT = 35;
    private static final int ANIMATION_SPEED = 20;
    private static final int MESSAGE_HEIGHT = 12;
    private static final int MAX_MESSAGES = 500;
    
    private static boolean isOpen = false;
    private static float slideOffset = PANEL_WIDTH; // Start fully hidden
    private static EditBox inputBox;
    private static List<String> commandHistory = new ArrayList<>();
    private static int historyIndex = -1;
    
    // Message history and scrolling
    private static List<ChatMessage> messages = new ArrayList<>();
    private static int scrollOffset = 0;
    private static int maxScroll = 0;
    private static final int BACKGROUND_COLOR = 0x15202020; // Ultra transparent (15 = ~8% opacity)
    private static final int BORDER_COLOR = 0x40404040; // More transparent border
    private static final int HEADER_COLOR = 0x25252525; // More transparent header (~15% opacity)
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    
    // Message bubble colors
    private static final int USER_BUBBLE_COLOR = 0xC04CAF50; // Green bubble for user
    private static final int STEVE_BUBBLE_COLOR = 0xC02196F3; // Blue bubble for Steve
    private static final int SYSTEM_BUBBLE_COLOR = 0xC0FF9800; // Orange bubble for system

    private SteveGUI() {
    }

    private static class ChatMessage {
        String sender; // "You", "Steve", "Alex", "System", etc.
        String text;
        int bubbleColor;
        boolean isUser; // true if message from user
        
        ChatMessage(String sender, String text, int bubbleColor, boolean isUser) {
            this.sender = sender;
            this.text = text;
            this.bubbleColor = bubbleColor;
            this.isUser = isUser;
        }
    }

    public static void toggle() {
        isOpen = !isOpen;
        
        Minecraft mc = Minecraft.getInstance();
        
        if (isOpen) {
            initializeInputBox();
            mc.setScreen(new SteveOverlayScreen());
            if (inputBox != null) {
                inputBox.setFocused(true);
            }
        } else {
            if (inputBox != null) {
                inputBox = null;
            }
            if (mc.screen instanceof SteveOverlayScreen) {
                mc.setScreen(null);
            }
        }
    }

    public static boolean isOpen() {
        return isOpen;
    }

    private static void initializeInputBox() {
        Minecraft mc = Minecraft.getInstance();
        if (inputBox == null) {
            inputBox = new EditBox(mc.font, 0, 0, PANEL_WIDTH - 20, 20, 
                Component.literal("Command"));
            inputBox.setMaxLength(256);
            inputBox.setHint(Component.literal("Tell Steve what to do..."));
            inputBox.setFocused(true);
        }
    }

    /**
     * Add a message to the chat history
     */
    public static void addMessage(String sender, String text, int bubbleColor, boolean isUser) {
        messages.add(new ChatMessage(sender, text, bubbleColor, isUser));
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
        // Auto-scroll to bottom on new message
        scrollOffset = 0;
    }

    /**
     * Add a user command to the history
     */
    public static void addUserMessage(String text) {
        addMessage("You", text, USER_BUBBLE_COLOR, true);
    }

    /**
     * Add a Steve response to the history
     */
    public static void addSteveMessage(String steveName, String text) {
        addMessage(steveName, text, STEVE_BUBBLE_COLOR, false);
    }

    /**
     * Add a system message to the history
     */
    public static void addSystemMessage(String text) {
        addMessage("System", text, SYSTEM_BUBBLE_COLOR, false);
    }

    public static void render(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        updateSlideOffset();
        if (slideOffset >= PANEL_WIDTH) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int panelX = (int) (screenWidth - PANEL_WIDTH + slideOffset);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.blendFuncSeparate(
            com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
            com.mojang.blaze3d.platform.GlStateManager.DestFactor.ZERO
        );

    drawPanelBackground(graphics, screenWidth, screenHeight, panelX);
    drawHeader(graphics, mc, panelX);

        int inputAreaY = screenHeight - 80;
        drawMessages(graphics, mc, panelX, screenWidth, inputAreaY);
        drawInputArea(graphics, mc, panelX, screenWidth, inputAreaY, tickDelta);

        RenderSystem.disableBlend();
    }

    private static void updateSlideOffset() {
        if (isOpen && slideOffset > 0) {
            slideOffset = Math.max(0, slideOffset - ANIMATION_SPEED);
        } else if (!isOpen && slideOffset < PANEL_WIDTH) {
            slideOffset = Math.min(PANEL_WIDTH, slideOffset + ANIMATION_SPEED);
        }
    }

    private static void drawPanelBackground(GuiGraphics graphics, int screenWidth, int screenHeight, int panelX) {
        graphics.fillGradient(panelX, 0, screenWidth, screenHeight, BACKGROUND_COLOR, BACKGROUND_COLOR);
        graphics.fillGradient(panelX - 2, 0, panelX, screenHeight, BORDER_COLOR, BORDER_COLOR);
        graphics.fillGradient(panelX, 0, screenWidth, HEADER_HEIGHT, HEADER_COLOR, HEADER_COLOR);
    }

    private static void drawHeader(GuiGraphics graphics, Minecraft mc, int panelX) {
        graphics.drawString(mc.font, "§lSteve AI", panelX + PANEL_PADDING, 8, TEXT_COLOR);
        graphics.drawString(mc.font, "§7Press K to close", panelX + PANEL_PADDING, 20, 0xFF888888);
    }

    private static void drawMessages(GuiGraphics graphics, Minecraft mc, int panelX, int screenWidth, int inputAreaY) {
        int messageAreaTop = HEADER_HEIGHT + 5;
        int messageAreaHeight = inputAreaY - messageAreaTop - 5;
        int messageAreaBottom = messageAreaTop + messageAreaHeight;

        int entryHeight = (MESSAGE_HEIGHT + 10) + 5 + 12;
        int totalMessageHeight = entryHeight * messages.size();

        maxScroll = Math.max(0, totalMessageHeight - messageAreaHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        graphics.enableScissor(panelX, messageAreaTop, panelX + PANEL_WIDTH, messageAreaBottom);

        if (messages.isEmpty()) {
            graphics.drawString(mc.font, "§7No messages yet...", panelX + PANEL_PADDING, messageAreaTop + 5, 0xFF666666);
            graphics.drawString(mc.font, "§7Type a command below!", panelX + PANEL_PADDING, messageAreaTop + 17, 0xFF555555);
        } else {
            int currentY = messageAreaBottom - 5;
            MessageRenderContext context = new MessageRenderContext(panelX, screenWidth, messageAreaTop, messageAreaBottom);
            for (int i = messages.size() - 1; i >= 0; i--) {
                ChatMessage msg = messages.get(i);
                renderMessage(graphics, mc, msg, context, currentY);
                currentY -= entryHeight;
            }
        }

        graphics.disableScissor();

        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, (messageAreaHeight * messageAreaHeight) / (maxScroll + messageAreaHeight));
            int scrollBarY = messageAreaTop + (int) ((messageAreaHeight - scrollBarHeight) * (1.0f - (float) scrollOffset / maxScroll));
            graphics.fill(screenWidth - 4, scrollBarY, screenWidth - 2, scrollBarY + scrollBarHeight, 0xFF888888);
        }
    }

    private static void renderMessage(GuiGraphics graphics, Minecraft mc, ChatMessage msg, MessageRenderContext context, int currentY) {
        int maxBubbleWidth = PANEL_WIDTH - (PANEL_PADDING * 3);
        String wrappedText = wrapText(mc.font, msg.text, maxBubbleWidth - 10);
        int textWidth = mc.font.width(wrappedText);
        int bubbleWidth = Math.min(textWidth + 10, maxBubbleWidth);
        int bubbleHeight = MESSAGE_HEIGHT + 10;

        int msgY = currentY - bubbleHeight + scrollOffset;
        if (msgY + bubbleHeight < context.messageAreaTop() - 20 || msgY > context.messageAreaBottom() + 20) {
            return;
        }

        if (msg.isUser) {
            int bubbleX = context.screenWidth() - bubbleWidth - PANEL_PADDING - 5;
            graphics.fillGradient(bubbleX - 3, msgY - 3, bubbleX + bubbleWidth + 3, msgY + bubbleHeight, msg.bubbleColor, msg.bubbleColor);
            graphics.drawString(mc.font, "§7" + msg.sender, bubbleX, msgY - 12, 0xFFCCCCCC);
            graphics.drawString(mc.font, wrappedText, bubbleX + 5, msgY + 5, 0xFFFFFFFF);
        } else {
            int bubbleX = context.panelX() + PANEL_PADDING;
            graphics.fillGradient(bubbleX - 3, msgY - 3, bubbleX + bubbleWidth + 3, msgY + bubbleHeight, msg.bubbleColor, msg.bubbleColor);
            graphics.drawString(mc.font, "§l" + msg.sender, bubbleX, msgY - 12, TEXT_COLOR);
            graphics.drawString(mc.font, wrappedText, bubbleX + 5, msgY + 5, 0xFFFFFFFF);
        }
    }

    private record MessageRenderContext(int panelX, int screenWidth, int messageAreaTop, int messageAreaBottom) {
    }

    private static void drawInputArea(GuiGraphics graphics, Minecraft mc, int panelX, int screenWidth, int inputAreaY, float tickDelta) {
        graphics.fillGradient(panelX, inputAreaY, screenWidth, mc.getWindow().getGuiScaledHeight(), HEADER_COLOR, HEADER_COLOR);
        graphics.drawString(mc.font, "§7Command:", panelX + PANEL_PADDING, inputAreaY + 10, 0xFF888888);

        if (inputBox != null && isOpen) {
            inputBox.setX(panelX + PANEL_PADDING);
            inputBox.setY(inputAreaY + 25);
            inputBox.setWidth(PANEL_WIDTH - (PANEL_PADDING * 2));
            inputBox.render(graphics, scaledMouseX(mc), scaledMouseY(mc), tickDelta);
        }

        graphics.drawString(mc.font, "§8Enter: Send | ↑↓: History | Scroll: Messages",
            panelX + PANEL_PADDING, mc.getWindow().getGuiScaledHeight() - 15, 0xFF555555);
    }

    private static int scaledMouseX(Minecraft mc) {
        double windowWidth = mc.getWindow().getScreenWidth();
        if (windowWidth <= 0) {
            return 0;
        }
        double scaled = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / windowWidth;
        return (int) scaled;
    }

    private static int scaledMouseY(Minecraft mc) {
        double windowHeight = mc.getWindow().getScreenHeight();
        if (windowHeight <= 0) {
            return 0;
        }
        double scaled = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / windowHeight;
        return (int) scaled;
    }

    /**
     * Simple word wrap for text
     */
    private static String wrapText(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        // Simple truncation for now
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(text.charAt(i));
            if (font.width(result.toString() + "...") >= maxWidth) {
                return result.substring(0, result.length() - 3) + "...";
            }
        }
        return result.toString();
    }

    public static boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        if (!isOpen || inputBox == null) return false;
        
        // Escape key - close panel
        if (keyCode == 256) { // ESC
            toggle();
            return true;
        }
        
        // Enter key - send command
        if (keyCode == 257) {
            String command = inputBox.getValue().trim();
            if (!command.isEmpty()) {
                sendCommand(command);
                inputBox.setValue("");
                historyIndex = -1;
            }
            return true;
        }

        // Arrow up - previous command
        if (keyCode == 265 && !commandHistory.isEmpty()) { // UP
            if (historyIndex < commandHistory.size() - 1) {
                historyIndex++;
                inputBox.setValue(commandHistory.get(commandHistory.size() - 1 - historyIndex));
            }
            return true;
        }

        // Arrow down - next command
        if (keyCode == 264) { // DOWN
            if (historyIndex > 0) {
                historyIndex--;
                inputBox.setValue(commandHistory.get(commandHistory.size() - 1 - historyIndex));
            } else if (historyIndex == 0) {
                historyIndex = -1;
                inputBox.setValue("");
            }
            return true;
        }

        // Backspace, Delete, Home, End, Left, Right - pass to input box
        if (keyCode == 259 || keyCode == 261 || keyCode == 268 || keyCode == 269 || 
            keyCode == 263 || keyCode == 262) {
            inputBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }

        return true; // Consume all keys to prevent game controls
    }

    public static boolean handleCharTyped(char codePoint, int modifiers) {
        if (isOpen && inputBox != null) {
            inputBox.charTyped(codePoint, modifiers);
            return true; // Consumed
        }
        return false;
    }

    public static void handleMouseClick(double mouseX, double mouseY, int button) {
        if (!isOpen || inputBox == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int panelX = (int) (screenWidth - PANEL_WIDTH + slideOffset);
        int panelRight = panelX + PANEL_WIDTH;

        int inputAreaY = mc.getWindow().getGuiScaledHeight() - 80;
        boolean insidePanel = mouseX >= panelX && mouseX <= panelRight;
        boolean insideField = mouseY >= inputAreaY + 25 && mouseY <= inputAreaY + 45;

        inputBox.setFocused(insidePanel && insideField && button == 0);
    }

    public static void handleMouseScroll(double scrollDelta) {
        if (!isOpen) return;
        
        int scrollAmount = (int)(scrollDelta * 3 * MESSAGE_HEIGHT);
        scrollOffset -= scrollAmount;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    private static void sendCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        appendHistory(command);
        addUserMessage(command);

        if (handleSpawnShortcut(mc, command)) {
            return;
        }

        List<String> targets = determineTargets(command);
        if (targets.isEmpty()) {
            addSystemMessage("No Steve agents found! Use 'spawn <name>' to create one.");
            return;
        }

        dispatchCommand(mc, targets, command);
    }

    private static void appendHistory(String command) {
        commandHistory.add(command);
        if (commandHistory.size() > 50) {
            commandHistory.remove(0);
        }
    }

    private static boolean handleSpawnShortcut(Minecraft mc, String command) {
        if (!command.toLowerCase().startsWith("spawn ")) {
            return false;
        }

        String name = command.substring(6).trim();
        if (name.isEmpty()) {
            name = "Steve";
        }

        if (mc.player != null) {
            mc.player.connection.sendCommand("steve spawn " + name);
            addSystemMessage("Spawning Steve agent: " + name);
        }
        return true;
    }

    private static List<String> determineTargets(String command) {
        List<String> targets = new ArrayList<>(parseTargetSteves(command));
        if (!targets.isEmpty()) {
            return targets;
        }

        var steves = SteveMod.getSteveManager().getAllSteves();
        if (!steves.isEmpty()) {
            targets.add(steves.iterator().next().getSteveName());
        }
        return targets;
    }

    private static void dispatchCommand(Minecraft mc, List<String> targets, String command) {
        if (mc.player == null) {
            return;
        }

        for (String steveName : targets) {
            mc.player.connection.sendCommand("steve tell " + steveName + " " + command);
        }

        String summary = targets.size() > 1
            ? "→ " + String.join(", ", targets) + ": " + command
            : "→ " + targets.get(0) + ": " + command;
        addSystemMessage(summary);
    }
    
    private static List<String> parseTargetSteves(String command) {
        List<String> targets = new ArrayList<>();
        String commandLower = command.toLowerCase();
        
        if (commandLower.startsWith("all steves ") || commandLower.startsWith("all ") || 
            commandLower.startsWith("everyone ") || commandLower.startsWith("everybody ")) {
            var allSteves = SteveMod.getSteveManager().getAllSteves();
            for (SteveEntity steve : allSteves) {
                targets.add(steve.getSteveName());
            }
            return targets;
        }
        
        var allSteves = SteveMod.getSteveManager().getAllSteves();
        List<String> availableNames = new ArrayList<>();
        for (SteveEntity steve : allSteves) {
            availableNames.add(steve.getSteveName().toLowerCase());
        }
        
        String[] parts = command.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            String firstWord = trimmed.split(" ")[0].toLowerCase();
            
            if (availableNames.contains(firstWord)) {
                for (SteveEntity steve : allSteves) {
                    if (steve.getSteveName().equalsIgnoreCase(firstWord)) {
                        targets.add(steve.getSteveName());
                        break;
                    }
                }
            }
        }
        
        return targets;
    }

    public static void tick() {
        if (isOpen && inputBox != null) {
            // Auto-focus input box when panel is open
            if (!inputBox.isFocused()) {
                inputBox.setFocused(true);
            }
        }
    }
}
