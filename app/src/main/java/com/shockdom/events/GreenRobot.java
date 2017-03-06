package com.shockdom.events;

import de.greenrobot.event.EventBus;

/**
 * Created by Walt on 20/05/2015.
 */
public class GreenRobot {

    private static EventBus instance;

    public static EventBus getEventBus() {
        if (instance == null)
            instance = new EventBus();
        return instance;
    }

}
