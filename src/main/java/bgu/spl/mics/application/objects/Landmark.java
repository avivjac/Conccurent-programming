package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class Landmark {

    //fields
    private String id;
    private String description;
    private List<CloudPoint> coordinates;

    //constructor
    public Landmark(String id, String description, List<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = coordinates;
    }

    //getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return "Landmark{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", coordinates=" + coordinates +
                '}';
    }
}