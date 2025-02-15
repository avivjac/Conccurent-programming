package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

/**
 * Manages the fusion of sensor data for simultaneous localization and mapping
 * (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update
 * a global map.
 */
public class FusionSlam {

    // Singleton instance holder
    private static class FusionSlamHolder {
        private static final FusionSlam INSTANCE = new FusionSlam(new ArrayList<>(), new ArrayList<>());
    }

    // Method to get the singleton instance
    public static FusionSlam getInstance() {
        return FusionSlamHolder.INSTANCE;
    }

    //fields
    private List<Landmark> landmarks;
    private List<Pose> poses;
    private AtomicInteger terminatedCount;

    //constructor
    public FusionSlam(List<Landmark> landmarks, List<Pose> poses) {
        this.landmarks = landmarks;
        this.poses = poses;
        terminatedCount = new AtomicInteger(0);
    }

    //getters and setters
    public List<Landmark> getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(List<Landmark> landmarks) {
        this.landmarks = landmarks;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public AtomicInteger getterminatedCount() {
        return terminatedCount;
    }

    public void setPoses(List<Pose> poses) {
        this.poses = poses;
    }

    /*
     * the function receives a list of TrackedObjects and transform them to Landmarks
     * 
     * @param trackedObjects - the list of TrackedObjects to process
     * 
     * INVARIENTS:
     * 1. the field landmarks has only valid Landmark objects.
     * -the landmarks correctly represent the environment and initialized correctly.
     * -the landmars are not null.
     * -the field landmarks is not null.
     * 2. the function is Thread safe.
     * 
     * PRE-CONDITIONS:
     * 1. The list of TrackedObjects is not null.
     * 
     * POST-CONDITIONS:
     * 1. The function will add new Landmarks to the map if the TrackedObject is new, else it will update the existing Landmark
     */
    public void processTrackedObjects(List<TrackedObject> trackedObjects, int tick) {
        for (TrackedObject trackedObject : trackedObjects) {
            // Transform the cloud points to the charging station's coordinate system using
            // the detection timestamp

            List<CloudPoint> transformedCoordinates = transformCoordinates(trackedObject.getCoordinates(),
                trackedObject.getTime());

            // Check if the object is new or previously detected
            Landmark existingLandmark = findLandmarkById(trackedObject.getId());
            if (existingLandmark != null) {
                // Update measurements by averaging with previous data
                List<CloudPoint> averagedCoordinates = averageCoordinates(existingLandmark.getCoordinates(),
                        transformedCoordinates);
                existingLandmark.setCoordinates(averagedCoordinates);
            } else {
                // Add new landmark to the map
                Landmark newLandmark = new Landmark(trackedObject.getId(), trackedObject.getDescription(),
                        transformedCoordinates);
                addLandmark(newLandmark);

                StatisticalFolder.getInstance().incrementLandmarks(); // update statistics
            }
        }
    }

    public void updatePose(Pose pose) {
        poses.add(pose);
    }

    private List<CloudPoint> transformCoordinates(List<CloudPoint> coordinates, int currenttime) {
        
        List<CloudPoint> transformedCoordinates = new ArrayList<>();
        Pose pose = getPoseAtTime(currenttime);
        // if (pose == null) {
        //     return transformedCoordinates; // or handle the case where pose is not found
        // }
        

        double theta = Math.toRadians(pose.getYaw());
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);
        double x0 = pose.getX();
        double y0 = pose.getY();

        for (CloudPoint point : coordinates) {
            double xr = point.getX();
            double yr = point.getY();
            double xg = x0 + xr * cosTheta - yr * sinTheta;
            double yg = y0 + xr * sinTheta + yr * cosTheta;
            transformedCoordinates.add(new CloudPoint(xg, yg));
        }

        return transformedCoordinates;
    }

    private Pose getPoseAtTime(int currenttime) {
        // Implement logic to find the pose at the given timestamp
        // This could involve interpolating between known poses
        // For simplicity, this example assumes poses are sorted by time
        for (Pose pose : poses) {
            if (pose != null) {
                if (pose.getTime() == currenttime) {
                    return pose;
                }
            }

        }
        return null;
    }

    private List<CloudPoint> averageCoordinates(List<CloudPoint> existingCoordinates, List<CloudPoint> newCoordinates) {
        List<CloudPoint> averagedCoordinates = new ArrayList<>();
        int size = Math.min(existingCoordinates.size(), newCoordinates.size());

        for (int i = 0; i < size; i++) {
            CloudPoint existingPoint = existingCoordinates.get(i);
            CloudPoint newPoint = newCoordinates.get(i);

            double avgX = (existingPoint.getX() + newPoint.getX()) / 2;
            double avgY = (existingPoint.getY() + newPoint.getY()) / 2;

            averagedCoordinates.add(new CloudPoint(avgX, avgY));
        }

        // Add any remaining new coordinates that don't have a corresponding existing
        // coordinate
        if (newCoordinates.size() > existingCoordinates.size()) {
            averagedCoordinates.addAll(newCoordinates.subList(existingCoordinates.size(), newCoordinates.size()));
        }

        return averagedCoordinates;
    }

    private Landmark findLandmarkById(String id) {
        for (Landmark landmark : landmarks) {
            if (landmark.getId().equals(id)) {
                return landmark;
            }
        }
        return null;
    }

    private void addLandmark(Landmark landmark) {
        landmarks.add(landmark);
    }

    @Override
    public String toString() {
        return "FusionSlam{" +
                "landmarks=" + landmarks +
                ", poses=" + poses +
                '}';
    }

}