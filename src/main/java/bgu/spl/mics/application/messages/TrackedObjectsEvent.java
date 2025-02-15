package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.List;

public class TrackedObjectsEvent implements Event<Object> {

    private final List<TrackedObject> trackedObjects;
    private final int detectionTime;

    public TrackedObjectsEvent(int detectionTime, List<TrackedObject> trackedObjects) {
        this.detectionTime = detectionTime;
        this.trackedObjects = trackedObjects;
    }

    public int getDetectionTime() {
        return detectionTime;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }
}