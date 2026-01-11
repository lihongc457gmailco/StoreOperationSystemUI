package com.mycompany.storeoperationsystemgui;

public class Session {
    //holds the ID of the user who logged in
    private static String currentUserId = "Unknown";

    public static void setCurrentUser(String userId) {
        currentUserId = userId;
    }

    public static String getCurrentUser() {
        return currentUserId;
    }
}