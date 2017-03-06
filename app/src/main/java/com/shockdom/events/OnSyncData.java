package com.shockdom.events;

/**
 * Created by walt on 30/05/15.
 */
public class OnSyncData {

    private boolean success;

    public OnSyncData(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
