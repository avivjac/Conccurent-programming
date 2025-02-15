package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single camera.
 */
public class Camera {
    // fields
    private int id;
    private int frequency;
    private STATUS status;
    private List<StampedDetectedObjects> stampedDetectedObjects;
    private StampedDetectedObjects lastDetectedObjects;

    //constructor
    public Camera(int id, int frequency, STATUS status, List<StampedDetectedObjects> detectedObjectsList) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.stampedDetectedObjects = detectedObjectsList;
        this.lastDetectedObjects = new StampedDetectedObjects(0, new ArrayList<>());
    }

    //constructor
    public Camera(int id, int frequency) {
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.stampedDetectedObjects = new ArrayList<>();
        this.lastDetectedObjects = null;
    }

    //getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<StampedDetectedObjects> getStampedDetectedObjects() {
        return stampedDetectedObjects;
    }

    public void setStampedDetectedObjects(List<StampedDetectedObjects> stampedDetectedObjects) {
        this.stampedDetectedObjects = stampedDetectedObjects;
    }

    public StampedDetectedObjects getLastDetectedObjects() {
        return lastDetectedObjects;

    }

    public void setLastDetectedObjects(StampedDetectedObjects lastDetectedObjects) {
        this.lastDetectedObjects = lastDetectedObjects;
    }

    public String getErrorDescription() {
        for (StampedDetectedObjects stampedDetectedObjects : stampedDetectedObjects) {
            for (DetectedObject detectedObjects : stampedDetectedObjects.getDetectedObjects()) {
                if (detectedObjects.getId().equals("ERROR")) {
                    return detectedObjects.getDescription();
                }
            }
        }

        return "";
    }

    public int MaxDetectTime() {
        int lastTime = this.stampedDetectedObjects.get(stampedDetectedObjects.size() - 1).getTime() + frequency;
        return lastTime;
    }

    @Override
    public String toString() {
        return "Camera{" +
                "id=" + id +
                ", frequency=" + frequency +
                ", status=" + status +
                ", detectedObjectsList=" + stampedDetectedObjects +
                '}';
    }

    /* 
     *The function returns a list of detected objects for a given time
     *
     * @param time - the time for which the detected objects are requested
     * @return a list of detected objects for the given time
     * 
     * @INVARIANT:
     * 1. the field this.stampedDetectedObjects is not null.
     * 2. all the objects in this.stampedDetectedObjects are valid.
     * 
     * @PRE-CONDITION:
     * 1. the input time must be a valid integer representing a meaningful timestamp.
     * 
     * @POST-CONDITION:
     * 1. the field this.stampedDetectedObjects isn't changed.
     * 2. The method always returns a non-null List<DetectedObject>.
     */
    public List<DetectedObject> GetDetectedObjectsByTime(int time) {
        List<DetectedObject> result = new ArrayList<>();
    
        // Iterate through the list of StampedDetectedObjects
        for (StampedDetectedObjects stampedObject : this.stampedDetectedObjects) {
            if (stampedObject.getTime() == time) {
                // If the time matches, add the detected objects to the result list
                result.addAll(stampedObject.getDetectedObjects());
            }
        }
        return result;  // Return the list of detected objects for the given time
}

    public static StampedDetectedObjects GetStampedDetectedObject(
            List<StampedDetectedObjects> stampedDetectedObjectsList, int time) {
        // Iterate through the list of StampedDetectedObjects
        for (StampedDetectedObjects stampedObject : stampedDetectedObjectsList) {
            if (stampedObject.getTime() == time) {
                // If the time matches, return the StampedDetectedObjects
                return stampedObject;
            }
        }

        return null; // Return null if no matching StampedDetectedObjects is found
    }
}