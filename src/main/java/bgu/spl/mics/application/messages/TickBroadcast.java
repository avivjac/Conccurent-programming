package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

/**
 * A broadcast message indicating the passage of a tick in the system.
 */
public class TickBroadcast implements Broadcast {
    // fields
    private final int tick;

    public TickBroadcast(int tick) {
        this.tick = tick;
    }

    public int getTick() {
        return tick;
    }
}