package bgu.spl.mics.application.config;

import java.util.List;
import java.util.Map;

public class CameraDataWrapper {
    private Map<String, List<List<CameraData>>> cameras;

    // Getters and setters
    public Map<String, List<List<CameraData>>> getCameras() {
        return cameras;
    }

    public void setCameras(Map<String, List<List<CameraData>>> cameras) {
        this.cameras = cameras;
    }

    @Override
    public String toString() {
        return "CameraDataWrapper{" +
                "cameras=" + cameras +
                '}';
    }
}
