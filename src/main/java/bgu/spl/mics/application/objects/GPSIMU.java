package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {

    // fields
    private int currentTick;
    private STATUS status;
    private List<Pose> poseList;

    public GPSIMU(int currentTick, STATUS status, List<Pose> poseList) {
        this.currentTick = currentTick;
        this.status = status;
        this.poseList = poseList;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public List<Pose> getPoseList() {
        return poseList;
    }

    public void setPoseList(List<Pose> poseList) {
        this.poseList = poseList;
    }

    public Pose getPoseByTime(int time) {
        for (Pose pose : poseList) {
            if (pose.getTime() == time) {
                return pose;
            }
        }
        return null;
    }

    public int MaxDetectTime() {
        int max = poseList.get(poseList.size() - 1).getTime();
        return max;
    }

    @Override
    public String toString() {
        return "GPSIMU{" +
                "currentTick=" + currentTick +
                ", status=" + status +
                ", poseList=" + poseList +
                '}';
    }
}