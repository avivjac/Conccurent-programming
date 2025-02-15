package bgu.spl.mics.application.config;

import java.util.List;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;

/**
 * Represents the configuration of the system.
 * An object of this class is created from the configuration file.
 * The class ConfigParser is helping class for this class.
 */
public class Configuration {
    // fields
    private Cameras Cameras;
    private Lidars Lidars;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    // Getters and setters
    public Cameras getCameras() {
        return Cameras;
    }

    public void setCameras(Cameras cameras) {
        Cameras = cameras;
    }

    public Lidars getLidars() {
        return Lidars;
    }

    public void setLidars(Lidars lidars) {
        Lidars = lidars;
    }

    public String getPoseJsonFile() {
        return poseJsonFile;
    }

    public void setPoseJsonFile(String poseJsonFile) {
        this.poseJsonFile = poseJsonFile;
    }

    public int getTickTime() {
        return TickTime;
    }

    public void setTickTime(int tickTime) {
        TickTime = tickTime;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "Cameras=" + Cameras +
                ", Lidars=" + Lidars +
                ", poseJsonFile='" + poseJsonFile + '\'' +
                ", TickTime=" + TickTime +
                ", Duration=" + Duration +
                '}';
    }

    // the function checks if thers crshed sensor and return it if there is
    // else the function return null
    public static Object isCrash(List<Camera> camers, List<LiDarWorkerTracker> lidars) {
        for (Camera camera : camers) {
            if (camera.getStatus() == STATUS.ERROR) {
                return camera;
            }
        }
        for (LiDarWorkerTracker lidar : lidars) {
            if (lidar.getStatus() == STATUS.ERROR) {
                return lidar;
            }
        }
        return null;
    }
}
