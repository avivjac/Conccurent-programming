package bgu.spl.mics.application.config;

import java.util.List;

public class Cameras {
    private List<CameraConfiguration> CamerasConfigurations;
    private String camera_datas_path;

    // Getters and setters
    public List<CameraConfiguration> getCamerasConfigurations() {
        return CamerasConfigurations;
    }

    public void setCamerasConfigurations(List<CameraConfiguration> camerasConfigurations) {
        CamerasConfigurations = camerasConfigurations;
    }

    public String getCamera_datas_path() {
        return camera_datas_path;
    }

    public void setCamera_datas_path(String camera_datas_path) {
        this.camera_datas_path = camera_datas_path;
    }

    @Override
    public String toString() {
        return "Cameras{" +
                "CamerasConfigurations=" + CamerasConfigurations +
                ", camera_datas_path='" + camera_datas_path + '\'' +
                '}';
    }
}
