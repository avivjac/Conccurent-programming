package bgu.spl.mics.application.config;

public class LidarConfiguration {
    private int id;
    private int frequency;

    // Getters and setters
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

    @Override
    public String toString() {
        return "LidarConfiguration{" +
                "id=" + id +
                ", frequency=" + frequency +
                '}';
    }
}
