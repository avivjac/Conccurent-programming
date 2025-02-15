package bgu.spl.mics.application;

import bgu.spl.mics.application.config.ConfigParser;
import bgu.spl.mics.application.config.Configuration;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import java.util.ArrayList;
import java.util.List;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {
    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the
     *             path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");

        try {
            if (args.length < 1) {
               throw new IllegalArgumentException("Missing configuration file path argument");
            }
            
            // 1. config parsing
            ConfigParser configParser = new ConfigParser(args[0]);
            //ConfigParser configParser = new ConfigParser("example_input_with_error\\configuration_file.json");
            List<Camera> cameras = configParser.getCameras();
            List<LiDarWorkerTracker> lidars = configParser.getLidars();
            List<Pose> poses = configParser.getPoses();

            // 2. create global services
            List<Thread> serviceThreads = new ArrayList<>();
            StatisticalFolder stats = StatisticalFolder.getInstance();

            // 3. create TimeService
            TimeService timeService = new TimeService(configParser.getTickTime(), configParser.getDuration());
            Thread timeServiceThread = new Thread(timeService, "TimeService");
            // serviceThreads.add(timeServiceThread);

            // 4. create CameraService
            for (Camera camera : cameras) {
                CameraService cameraService = new CameraService(camera);
                Thread cameraThread = new Thread(cameraService, "Camera-" + camera.getId());
                serviceThreads.add(cameraThread);
            }

            // 5. create LidarWorkerService
            for (LiDarWorkerTracker worker : lidars) {
                LiDarService LiDarService = new LiDarService(worker, stats);
                Thread LiDarThread = new Thread(LiDarService, "LiDar-" + worker.getId());
                serviceThreads.add(LiDarThread);
            }

            // 6. create PoseService
            GPSIMU gps = new GPSIMU(0, STATUS.DOWN, poses);
            PoseService poseService = new PoseService(gps);
            Thread poseThread = new Thread(poseService, "PoseService");
            serviceThreads.add(poseThread);

            // 7. create FusionSlamService
            FusionSlam fusionSlam = FusionSlam.getInstance();
            FusionSlamService fusionService = new FusionSlamService(fusionSlam, serviceThreads.size());
            Thread fusionThread = new Thread(fusionService, "FusionService");
            serviceThreads.add(fusionThread);

            // 8. Activate all services
            System.out.println("Starting all services...");
            for (Thread thread : serviceThreads) {
                thread.start();
            }
            timeServiceThread.start();

            // 9. Wait for all to be done
            for (Thread thread : serviceThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            timeServiceThread.join();



            // Access Fusion SLAM and create the output file
            System.out.println("\nAll threads are finished.");
            // 10. create output file
            System.out.println("\nCreating output file...");
            String outputPath = "output_file.json";

            Object isCrash = Configuration.isCrash(cameras, lidars);

            if (isCrash != null) {
                outputPath = "OutputError.json";
                configParser.createErrorOutputFile(outputPath, stats, FusionSlam.getInstance().getLandmarks(), isCrash,
                        cameras, lidars);
            } else { // if no crash
                outputPath = "output_file.json";
                configParser.createOutputFile(outputPath, stats, FusionSlam.getInstance().getLandmarks());
            }

            System.out.println("### output File LINK ###");
            System.out.println(outputPath.toString());
            System.out.println("### output File LINK ###");
        }
         catch (Exception e) {
            e.printStackTrace();
        }
    
    }
}