package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * FusionSlamService is responsible for managing the environmental map by
 * processing tracked objects.
 * It subscribes to TickBroadcast, TrackedObjectsEvent, PoseEvent,
 * TerminatedBroadcast, and CrashedBroadcast.
 */
public class FusionSlamService extends MicroService {

    private final FusionSlam fusionSlam;
    private int totalServices; // Total number of services to wait for termination
    private List<TrackedObjectsEvent> trackedObjectsEvents;


    public FusionSlamService(FusionSlam fusionSlam, int totalServices) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
        this.totalServices = totalServices;
        this.trackedObjectsEvents = new ArrayList<>();
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents,
     * TickBroadcasts,
     * TerminatedBroadcasts, and CrashedBroadcasts, and sets up callbacks for
     * updating the global map.
     */
    @Override
    protected void initialize() {

        subscribeBroadcast(TerminatedBroadcast.class, broadcast -> {
            this.decrementTotalServices(1);
            if (totalServices <= 0) 
            {
                System.out.println("FusionSlamService terminating...");
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                terminate();
                sendBroadcast(new TerminatedBroadcast("FusionSlamService"));
                return;
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, broadcast -> {
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, tick -> {
            System.out.println("FusionSlamService at tick " + tick.getTick());
            // update statistics
            if (totalServices <= 0) {
                System.out.println("FusionSlamService terminating...");
                terminate();
                sendBroadcast(new TerminatedBroadcast("FusionSlamService"));
                return;
            }
            if (tick.getTick() == -1) {
                System.out.println("FusionSlamService terminating...");
                terminate();
                return;
            }

        });

        subscribeEvent(TrackedObjectsEvent.class, event -> {
            // Process TrackedObjectsEvent
            trackedObjectsEvents.add(event);

             Iterator<TrackedObjectsEvent> iterator = trackedObjectsEvents.iterator();
                while (iterator.hasNext()) {
                    TrackedObjectsEvent e = iterator.next();
                    if (fusionSlam.getPoses().size()>e.getDetectionTime()) {
                        fusionSlam.processTrackedObjects(e.getTrackedObjects(), e.getDetectionTime());
                        iterator.remove();
                    }
                }
            System.out.println("FusionSlamService processing TrackedObjectsEvent at time " + event.getDetectionTime());


            complete(event, event.getTrackedObjects());
        });

        subscribeEvent(PoseEvent.class, event -> {
            // Process PoseEvent
            Pose pose = event.getPose();
            fusionSlam.updatePose(pose);
            complete(event, pose);
        });
    }

    public void decrementTotalServices(int num) {
        this.totalServices -= num;
    }

}