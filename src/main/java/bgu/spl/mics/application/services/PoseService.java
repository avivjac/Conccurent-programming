package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for holding the robot's coordinates at each tick,
 * sending PoseEvents, and subscribing to TickBroadcast.
 */
public class PoseService extends MicroService {

    // fields
    private GPSIMU gpsimu;
    private Pose currentPose;
    private int currentTick;

    public PoseService(GPSIMU gpsimu) {
        super("Pose");
        this.gpsimu = gpsimu;
        this.currentTick = 0;
        if (gpsimu.getPoseList().size() == 0) {
            this.currentPose = new Pose(0, 0, 0, 0);
        } else {
            this.currentPose = gpsimu.getPoseByTime(currentTick);
            ;
        }
    }

    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the
     * current pose.
     */
    @Override
    protected void initialize() {

        subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
            @Override
            public void call(TerminatedBroadcast broadcast) {
                // Handle termination
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            @Override
            public void call(CrashedBroadcast broadcast) {
                // Handle crash
                gpsimu.setStatus(STATUS.ERROR);
                terminate();
            }
        });

        subscribeBroadcast(TickBroadcast.class, tick -> {
            if (tick.getTick() == -1) {
                System.out.println("PoseService terminating...");
                terminate();
                return;
            }
            currentTick = tick.getTick();
            System.out.println("PoseService at tick " + currentTick);

            if (gpsimu.MaxDetectTime() < currentTick) {
                sendBroadcast(new TerminatedBroadcast("gpsImu"));
                terminate();
                return;
            } else {

                // Get the current pose from the GPSIMU
                currentPose = gpsimu.getPoseByTime(currentTick);

                // Send a PoseEvent with the current pose
                sendEvent(new PoseEvent(currentPose));
                System.out.println("PoseService sent pose update at tick " + currentTick);

            }
        });

    }
}