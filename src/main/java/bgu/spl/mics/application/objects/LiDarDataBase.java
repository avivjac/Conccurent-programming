package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.application.config.LidarData;

/**
 * Singleton class that manages LiDAR data.
 */
public class LiDarDataBase {
    // Fields
    private List<StampedCloudPoints> cloudPoints;

    // Private class to hold the instance of the singleton
    private static class DataBaseHolder {
        private static final LiDarDataBase INSTANCE = new LiDarDataBase();
    }

    // constructor
    public LiDarDataBase() {
        this.cloudPoints = new ArrayList<>();
    }

    // Method to get the singleton instance
    public static LiDarDataBase getInstance() {
        return DataBaseHolder.INSTANCE;
    }

    // get cloud points
    public List<StampedCloudPoints> getCloudPoints() {
        return cloudPoints;
    }

    // setters
    public void setCloudPoints(List<StampedCloudPoints> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }

    public static List<StampedCloudPoints> lidarDataToStamped(List<LidarData> data) {
        List<StampedCloudPoints> output = new ArrayList<>();
        for (LidarData obj : data) {
            output.add(new StampedCloudPoints(obj.getId(), obj.getTime(), obj.getCloudPoints()));
        }
        return output;
    }

    public int MaxTrackCloudPointsTime() {
        int max = cloudPoints.get(cloudPoints.size() - 1).getTime();
        return max;
    }
}