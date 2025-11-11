package com.steve.ai.refabricated.util;

/**
 * Proxy for client-side operations that can be safely called from common code.
 * Methods will only execute on the client side.
 */
public class ClientProxy {
    
    /**
     * Add a message to the Steve GUI (client-side only).
     * This is a no-op if called on the server or if the client class is not available.
     */
    public static void addSteveMessage(String steveName, String message) {
        // This will be implemented via reflection or a service loader to avoid
        // direct references to client classes from common code
        try {
            Class<?> steveGUIClass = Class.forName("com.steve.ai.refabricated.client.SteveGUI");
            steveGUIClass.getMethod("addSteveMessage", String.class, String.class)
                .invoke(null, steveName, message);
        } catch (Exception e) {
            // Silently fail - client classes not available (running on server)
        }
    }
}
