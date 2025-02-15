package bgu.spl.mics.application.config;

import java.util.List;
import bgu.spl.mics.application.objects.DetectedObject;

public class CameraData {
    private int time;
    private List<DetectedObject> detectedObjects;

    // Getters and setters
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<DetectedObject> getDetectedObjects() {
        return detectedObjects;
    }

    public void setDetectedObjects(List<DetectedObject> detectedObjects) {
        this.detectedObjects = detectedObjects;
    }

    @Override
    public String toString() {
        return "CameraData{" +
                "time=" + time +
                ", detectedObjects=" + detectedObjects +
                '}';
    }
}