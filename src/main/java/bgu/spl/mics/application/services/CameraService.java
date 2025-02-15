package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.LinkedList;
import java.util.List;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    // fields
    private final Camera camera;
    private int currentTick;
    private List<DetectedObjectsEvent> events;

    // constructor
    public CameraService(Camera camera) {
        super("Camera");
        this.camera = camera;
        this.currentTick = 0;
        this.events = new LinkedList<>();
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for
     * sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {

        System.out.println("Camera " + camera.getId() + " starting...");

        subscribeBroadcast(TickBroadcast.class, tick -> {
            System.out.println("camera " + camera.getId()
                    + " at tick " + tick.getTick());

            if (camera.MaxDetectTime() < currentTick) {
                sendBroadcast(new TerminatedBroadcast("camera" + camera.getId()));
                terminate();
                if (tick.getTick() == -1) {
                    System.out.println("Camera " + camera.getId() + " terminating...");
                    terminate();
                    return;
                }
                return;
            } else {

                if (camera.getStatus() == STATUS.UP) {

                    currentTick = tick.getTick();
                    List<DetectedObject> cutrrentTimeDetectedObjects = camera.GetDetectedObjectsByTime(
                            currentTick);
                    StampedDetectedObjects detectedAtTime = new StampedDetectedObjects(currentTick,
                            cutrrentTimeDetectedObjects);

                    if (detectedAtTime.getDetectedObjects().size() > 0) {
                        for (DetectedObject detectedObject : detectedAtTime.getDetectedObjects()) {
                            if (detectedObject.getId().equals("ERROR")) {
                                CrashedBroadcast crashedBroadcast = new CrashedBroadcast("Camera" + camera.getId());
                                sendBroadcast(crashedBroadcast);
                                System.out
                                        .println("Camera " + camera.getId() + " received ERROR at tick " + currentTick);
                                return;
                            } else {
                                StatisticalFolder.getInstance().incrementDetectedObjects();

                            }
                        }

                        this.camera.setLastDetectedObjects(detectedAtTime); // set the last detected objects
                        DetectedObjectsEvent detectedObjectsEvent = new DetectedObjectsEvent(currentTick,
                                detectedAtTime);
                        this.events.add(detectedObjectsEvent);

                        for (DetectedObjectsEvent e : this.events) {
                            if (e.getDetectionTime() + this.camera.getFrequency() >= currentTick) {
                                sendEvent(e);
                                this.events.remove(e);
                            }
                        }
                    }
                }
            }
        });

        subscribeBroadcast(TerminatedBroadcast.class, new Callback<TerminatedBroadcast>() {
            @Override
            public void call(TerminatedBroadcast terminated) {
            }
        });

        subscribeBroadcast(CrashedBroadcast.class, new Callback<CrashedBroadcast>() {
            @Override
            public void call(CrashedBroadcast crashed) {
                // Change status to ERROR and terminate
                camera.setStatus(STATUS.ERROR);
                terminate();
            }
        });
    }
    // end initialize

}