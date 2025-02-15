package bgu.spl.mics.application.config;

import bgu.spl.mics.application.objects.CloudPoint;

import java.util.List;

public class LidarData {
    private int time;
    private String id;
    private List<CloudPoint> cloudPoints;

    // Getters and setters
    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CloudPoint> getCloudPoints() {
        return cloudPoints;
    }

    public void setCloudPoints(List<CloudPoint> cloudPoints) {
        this.cloudPoints = cloudPoints;
    }

    @Override
    public String toString() {
        return "LidarData{" +
                "time=" + time +
                ", id='" + id + '\'' +
                ", cloudPoints=" + cloudPoints +
                '}';
    }
}
