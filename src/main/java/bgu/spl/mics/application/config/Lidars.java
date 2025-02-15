package bgu.spl.mics.application.config;

import java.util.List;

public class Lidars {
    private List<LidarConfiguration> LidarConfigurations;
    private String lidars_data_path;

    // Getters and setters
    public List<LidarConfiguration> getLidarConfigurations() {
        return LidarConfigurations;
    }

    public void setLidarConfigurations(List<LidarConfiguration> lidarConfigurations) {
        LidarConfigurations = lidarConfigurations;
    }

    public String getLidars_data_path() {
        return lidars_data_path;
    }

    public void setLidars_data_path(String lidars_data_path) {
        this.lidars_data_path = lidars_data_path;
    }

    @Override
    public String toString() {
        return "Lidars{" +
                "LidarConfigurations=" + LidarConfigurations +
                ", lidars_data_path='" + lidars_data_path + '\'' +
                '}';
    }
}