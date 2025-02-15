package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * An event representing the detection of objects by the camera.
 */
public class DetectedObjectsEvent implements Event<Boolean> {
    // fields
    private final StampedDetectedObjects stampedDetectedObjects;
    private final int detectionTime;

    // constructor
    public DetectedObjectsEvent(int detectionTime, StampedDetectedObjects detectedObjects) {
        this.detectionTime = detectionTime;
        this.stampedDetectedObjects = detectedObjects;
    }

    public int getDetectionTime() {
        return detectionTime;
    }

    public StampedDetectedObjects getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }
}