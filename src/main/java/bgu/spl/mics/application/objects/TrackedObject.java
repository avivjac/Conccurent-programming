package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description,
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {

    // fields
    private String id;
    private int time;
    private String description;
    private List<CloudPoint> coordinates;

    // constructors
    public TrackedObject(String id, int time, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = coordinates;
    }

    public TrackedObject(String id, int time, String description) {
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = new ArrayList<>();
    }

    public TrackedObject() {
    }

    public synchronized void add(CloudPoint point) {
        coordinates.add(point);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CloudPoint> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<CloudPoint> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "id='" + id +
                ", time=" + time +
                ", description='" + description +
                ", coordinates=" + coordinates.toString();
    }
}