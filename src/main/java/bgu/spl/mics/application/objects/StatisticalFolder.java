package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

public class StatisticalFolder {
    private static StatisticalFolder instance;
    private final AtomicInteger systemRuntime;
    private final AtomicInteger numDetectedObjects;
    private final AtomicInteger numTrackedObjects;
    private final AtomicInteger numLandmarks;

    private StatisticalFolder() {
        this.systemRuntime = new AtomicInteger(0);
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandmarks = new AtomicInteger(0);
    }

    public static synchronized StatisticalFolder getInstance() {
        if (instance == null) {
            instance = new StatisticalFolder();
        }
        return instance;
    }

    public void incrementSystemRuntime() {
        systemRuntime.incrementAndGet();
    }

    public void incrementDetectedObjects() {
        numDetectedObjects.incrementAndGet();
    }

    public void incrementTrackedObjects() {
        numTrackedObjects.incrementAndGet();
    }

    public void incrementLandmarks() {
        numLandmarks.incrementAndGet();
    }

    public int getSystemRuntime() {
        return systemRuntime.get();
    }

    public int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public int getNumLandmarks() {
        return numLandmarks.get();
    }

    public synchronized void setSystemRuntime(int runtime) {
        this.systemRuntime.set(runtime);
    }

    // Method to update statistics from cameras and LiDAR workers
    public void updateStatistics(int detectedObjects, int trackedObjects, int landmarks) {
        numDetectedObjects.addAndGet(detectedObjects);
        numTrackedObjects.addAndGet(trackedObjects);
        numLandmarks.addAndGet(landmarks);
    }
}