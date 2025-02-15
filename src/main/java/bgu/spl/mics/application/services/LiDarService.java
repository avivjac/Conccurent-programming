package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LiDarService extends MicroService {
    // fields
    private LiDarWorkerTracker liDarWorkerTracker;
    private StatisticalFolder statisticalFolder;
    private int currentTick;
    private List<TrackedObjectsEvent> trackedObjectsEvents;

    public LiDarService(LiDarWorkerTracker liDarWorkerTracker, StatisticalFolder statisticalFolder) {
        super("LiDarService");
        this.liDarWorkerTracker = liDarWorkerTracker;
        this.statisticalFolder = statisticalFolder;
        this.currentTick = 0;
        this.trackedObjectsEvents = new LinkedList<>();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        System.out.println("LiDAR " + liDarWorkerTracker.getId() + " starting...");

        subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
            @Override
            public void call(TerminatedBroadcast broadcast) {
                // Handle termination
            }
        });

        subscribeBroadcast(TickBroadcast.class, tick -> {
            System.out.println("lidar " + liDarWorkerTracker.getId()
                    + " at tick " + tick.getTick());
            currentTick = tick.getTick();
            if (liDarWorkerTracker.MaxTrackedTime() < currentTick) {
                sendBroadcast(new TerminatedBroadcast("LiDAR" + liDarWorkerTracker.getId()));
                terminate();
                return;
            } else {
                if (tick.getTick() == -1) {
                    System.out.println("LiDAR " + liDarWorkerTracker.getId() + " terminating...");
                    terminate();
                    return;
                }

            }
        });

        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            @Override
            public void call(CrashedBroadcast broadcast) {
                // Handle crash
                liDarWorkerTracker.setStatus(STATUS.ERROR);
                terminate();
            }
        });

        subscribeEvent(DetectedObjectsEvent.class, new Callback<DetectedObjectsEvent>() {
            @Override
            public void call(DetectedObjectsEvent event) {
                System.out.println("LiDAR " + liDarWorkerTracker.getId() + " received DetectedObjectsEvent at tick "
                        + event.getDetectionTime());
                List<TrackedObject> listTrackedObjects = new ArrayList<>();
                List<StampedCloudPoints> l = LiDarDataBase.getInstance().getCloudPoints();
                List<StampedCloudPoints> lByTime = LiDarWorkerTracker.cloudpointByTick(l, event.getDetectionTime());

                for (StampedCloudPoints isError : lByTime) {
                    if (isError.getId().equals("ERROR")) {
                        System.out.println("LiDAR " + liDarWorkerTracker.getId() + " received ERROR at tick "
                                + event.getDetectionTime());
                        CrashedBroadcast crashedBroadcast = new CrashedBroadcast("LiDar" + liDarWorkerTracker.getId());
                        sendBroadcast(crashedBroadcast);
                        System.out.println("LiDAR " + liDarWorkerTracker.getId() + " received ERROR at tick "
                                + event.getDetectionTime());
                        return;
                    }
                }

                for (DetectedObject detectedobject : event.getStampedDetectedObjects().getDetectedObjects()) {
                    List<CloudPoint> cloudPoints = LiDarWorkerTracker.getCloudPointsByStampedCloudPoins(lByTime,
                            detectedobject.getId());
                    if (cloudPoints != null) {
                        listTrackedObjects.add(
                                new TrackedObject(detectedobject.getId(), event.getStampedDetectedObjects().getTime(),
                                        detectedobject.getDescription(), cloudPoints));
                        statisticalFolder.incrementTrackedObjects();
                    }

                }

                TrackedObjectsEvent toe = new TrackedObjectsEvent(event.getStampedDetectedObjects().getTime(),
                        listTrackedObjects);
                trackedObjectsEvents.add(toe);

                liDarWorkerTracker.addTrackedObject(listTrackedObjects);

                if (listTrackedObjects.size() > 0) {
                    liDarWorkerTracker.setLastTrackedObject(listTrackedObjects); // set the last tracked objects
                }

                // Use an iterator to safely remove elements while iterating
                Iterator<TrackedObjectsEvent> iterator = trackedObjectsEvents.iterator();
                while (iterator.hasNext()) {
                    TrackedObjectsEvent e = iterator.next();
                    if (e.getDetectionTime() + liDarWorkerTracker.getFrequancy() <= currentTick) {
                        sendEvent(toe);
                        iterator.remove();
                    }
                }
            }
        });

    } // end initialize
}
