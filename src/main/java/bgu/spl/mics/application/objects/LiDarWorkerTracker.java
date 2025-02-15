package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using
 * data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    // fields
    private int id;
    private STATUS status;
    private int frequancy;
    private List<TrackedObject> trackedObjects;
    private List<TrackedObject> lastTrackedObject;

    // constructor
    public LiDarWorkerTracker(int id, int frequancy) {
        this.id = id;
        this.frequancy = frequancy;
        this.status = STATUS.UP;
        this.trackedObjects = new LinkedList<TrackedObject>();
        this.lastTrackedObject = new LinkedList<TrackedObject>();
    }

    // getters and setters
    public int getId() {
        return id;
    }

    public int getFrequancy() {
        return frequancy;
    }

    public STATUS getStatus() {
        return status;
    }

    public List<TrackedObject> getTrackedObjects() {
        return trackedObjects;
    }

    public List<TrackedObject> getLastTrackedObject() {
        return lastTrackedObject;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFrequancy(int frequancy) {
        this.frequancy = frequancy;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void setTrackedObjects(List<TrackedObject> trackedObjects) {
        this.trackedObjects = trackedObjects;
    }

    public void setLastTrackedObject(List<TrackedObject> lastTrackedObject) {
        this.lastTrackedObject = lastTrackedObject;
    }

    // addTrackedObject - add a tracked object to the list of tracked objects
    public void addTrackedObject(List<TrackedObject> trackedObject) {
        this.trackedObjects.addAll(trackedObject);
    }

    // tosString
    @Override
    public String toString() {
        return "LiDarWorkerTracker{" +
                "id=" + id +
                ", status=" + status +
                ", frequancy=" + frequancy +
                ", trackedObjects=" + trackedObjects +
                '}';
    }

    /*
     * the function gets a list of StampedCloudPoints and a tick and returns a list of StampedCloudPoints that their time is equal to the tick
     *
     * @param data - a list of StampedCloudPoints
     * @param tick - the time we want to get the StampedCloudPoints from
     * 
     * @return a list of StampedCloudPoints that their time is equal to the tick
     * 
     * @INVARIANT:
     * 1. the parameter data is not null.
     * 2. all the stampedCloudPoints in data are valid.
     * 3. the returned list must only contain StampedCloudPoints from the input list with getTime() == tick.
     * 
     * @PRE-CONDITION:
     * 1. the input tick must be a valid integer representing a meaningful timestamp.
     * 
     * @POST-CONDITION:
     * 1. the method always returns a non-null List<DetectedObject>.
     * 2. if no StampedCloudPoints match the given tick, the returned list will be empty.
     */
    public static List<StampedCloudPoints> cloudpointByTick(List<StampedCloudPoints> data, int tick) {
        List<StampedCloudPoints> output = new ArrayList<>();

        for (StampedCloudPoints stampedCloudPoints : data) {
            if (stampedCloudPoints.getTime() == tick)
                output.add(stampedCloudPoints);
        }
        return output;
    }

    public static List<CloudPoint> getCloudPointsByStampedCloudPoins(List<StampedCloudPoints> data, String id) {
        for (StampedCloudPoints stampedCloudPoints : data) {
            if (stampedCloudPoints.getId().equals(id))
                return stampedCloudPoints.getCloudPoints();
        }
        return null;
    }

    public int MaxTrackedTime() {
        return LiDarDataBase.getInstance().MaxTrackCloudPointsTime() + getFrequancy();
    }
}
