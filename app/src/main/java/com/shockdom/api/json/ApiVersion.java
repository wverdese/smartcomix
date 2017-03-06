package com.shockdom.api.json;

public class ApiVersion {

    private int minVersion;
    private boolean isValidClient;
    private String message;

    public ApiVersion(int minVersion, boolean isValidClient, String message) {
        this.minVersion = minVersion;
        this.isValidClient = isValidClient;
        this.message = message;
    }

    public int getMinVersion() {
        return minVersion;
    }

    public boolean isValidClient() {
        return isValidClient;
    }

    public String getMessage() {
        return message;
    }
}
