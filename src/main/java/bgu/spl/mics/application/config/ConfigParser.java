package bgu.spl.mics.application.config;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Landmark;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedCloudPoints;

/**
 * A class that parses a JSON configuration file using Gson.
 * The class configuration is helping class for this class.
 * All the funbctions in this calss are static and synchronized.
 */
public class ConfigParser {
    // fields
    private List<Camera> cameras;
    private List<LiDarWorkerTracker> lidars;
    private List<Pose> poses;
    private int tickTime;
    private int duration;
    private String path;

    // constructor
    public ConfigParser(String filePath) {
        Configuration config = JsonConfigParser(filePath);
        this.tickTime = config.getTickTime()*10; // convert the time to milliseconds
        this.duration = config.getDuration();

        // adjust the file paths
        this.path = getPath(filePath);
        String poseJsonFile = path + config.getPoseJsonFile().substring(2);
        String camerasDataPath = path + config.getCameras().getCamera_datas_path().substring(2);
        String lidarsDataPath = path + config.getLidars().getLidars_data_path().substring(2);


        this.poses = ConfigParser.PoseConfigParser(poseJsonFile); // initialize the poses
        CameraDataWrapper cameraDataWrapper = ConfigParser.CameraConfigParser(camerasDataPath);
        List<LidarData> lidarData = ConfigParser.parseLidarData(lidarsDataPath);
        // initialize the cameras and lidars
        this.cameras = ConfigParser.initCameras(config.getCameras().getCamerasConfigurations(),
                cameraDataWrapper.getCameras());
        this.lidars = ConfigParser.initLiDars(config.getLidars().getLidarConfigurations());
        LiDarDataBase ld = LiDarDataBase.getInstance(); // create the LiDarDataBase
        ld.setCloudPoints(ConfigParser.initCloudPoints(lidarData));
    }

    // Getters
    public List<Camera> getCameras() {
        return cameras;
    }

    public List<LiDarWorkerTracker> getLidars() {
        return lidars;
    }

    public List<Pose> getPoses() {
        return poses;
    }

    public int getTickTime() {
        return tickTime;
    }

    public int getDuration() {
        return duration;
    }

    // cut the path to the configuration file
    private String getPath(String filePath) {
        int indexOf = filePath.indexOf("configuration_file.json");
        String path = "";

        if (indexOf != -1) {
            path = filePath.substring(0, indexOf);
        }

        return path;
    }

    @Override
    public String toString() {
        return "ConfigParser{" +
                "cameras=" + cameras +
                ", lidars=" + lidars +
                ", poses=" + poses +
                ", tickTime=" + tickTime +
                ", duration=" + duration +
                '}';
    }

    public static synchronized Configuration JsonConfigParser(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            Configuration config = new Configuration();

            // Parse Cameras
            JsonObject camerasObject = jsonObject.getAsJsonObject("Cameras");
            Cameras cameras = new Cameras();
            JsonArray camerasConfigurationsArray = camerasObject.getAsJsonArray("CamerasConfigurations");
            List<CameraConfiguration> cameraConfigurations = new ArrayList<>();
            for (JsonElement element : camerasConfigurationsArray) {
                JsonObject cameraObject = element.getAsJsonObject();
                CameraConfiguration cameraConfig = new CameraConfiguration();
                cameraConfig.setId(cameraObject.get("id").getAsInt());
                cameraConfig.setFrequency(cameraObject.get("frequency").getAsInt());
                cameraConfig.setCamera_key(cameraObject.get("camera_key").getAsString());
                cameraConfigurations.add(cameraConfig);
            }

            cameras.setCamerasConfigurations(cameraConfigurations);
            cameras.setCamera_datas_path(camerasObject.get("camera_datas_path").getAsString());
            config.setCameras(cameras);

            // Parse Lidars
            JsonObject lidarsObject = jsonObject.getAsJsonObject("LiDarWorkers");
            if (lidarsObject != null) {
                Lidars lidars = new Lidars();
                JsonElement lidarConfigurationsElement = lidarsObject.get("LidarConfigurations");
                List<LidarConfiguration> lidarConfigurations = new ArrayList<>();

                if (lidarConfigurationsElement.isJsonArray()) {
                    JsonArray lidarsConfigurationsArray = lidarConfigurationsElement.getAsJsonArray();
                    for (JsonElement element : lidarsConfigurationsArray) {
                        JsonObject lidarObject = element.getAsJsonObject();
                        LidarConfiguration lidarConfig = new LidarConfiguration();
                        lidarConfig.setId(lidarObject.get("id").getAsInt());
                        lidarConfig.setFrequency(lidarObject.get("frequency").getAsInt());
                        lidarConfigurations.add(lidarConfig);
                    }
                } else if (lidarConfigurationsElement.isJsonObject()) {
                    JsonObject lidarObject = lidarConfigurationsElement.getAsJsonObject();
                    LidarConfiguration lidarConfig = new LidarConfiguration();
                    lidarConfig.setId(lidarObject.get("id").getAsInt());
                    lidarConfig.setFrequency(lidarObject.get("frequency").getAsInt());
                    lidarConfigurations.add(lidarConfig);
                }

                lidars.setLidarConfigurations(lidarConfigurations);
                lidars.setLidars_data_path(lidarsObject.get("lidars_data_path").getAsString());
                config.setLidars(lidars);
            }

            // Parse other fields
            config.setPoseJsonFile(jsonObject.get("poseJsonFile").getAsString());
            config.setTickTime(jsonObject.get("TickTime").getAsInt());
            config.setDuration(jsonObject.get("Duration").getAsInt());

            return config;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<LidarData> parseLidarData(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            List<LidarData> lidarDataList = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                LidarData lidarData = new LidarData();
                lidarData.setTime(jsonObject.get("time").getAsInt());
                lidarData.setId(jsonObject.get("id").getAsString());

                JsonArray cloudPointsArray = jsonObject.getAsJsonArray("cloudPoints");
                List<CloudPoint> cloudPoints = new ArrayList<>();
                for (JsonElement pointElement : cloudPointsArray) {
                    JsonArray pointArray = pointElement.getAsJsonArray();
                    double x = pointArray.get(0).getAsDouble();
                    double y = pointArray.get(1).getAsDouble();
                    CloudPoint cloudPoint = new CloudPoint(x, y);
                    cloudPoints.add(cloudPoint);
                }
                lidarData.setCloudPoints(cloudPoints);
                lidarDataList.add(lidarData);
            }

            return lidarDataList;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized CameraDataWrapper CameraConfigParser(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            CameraDataWrapper cameraDataWrapper = new CameraDataWrapper();
            Map<String, List<List<CameraData>>> cameras = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String cameraKey = entry.getKey();
                JsonElement cameraDataElement = entry.getValue();
                List<List<CameraData>> cameraDataList = new ArrayList<>();

                if (cameraDataElement.isJsonArray()) {
                    for (JsonElement element : cameraDataElement.getAsJsonArray()) {
                        List<CameraData> innerList = parseCameraDataObject(element.getAsJsonObject());
                        cameraDataList.add(innerList);
                    }
                } else if (cameraDataElement.isJsonObject()) {
                    List<CameraData> innerList = parseCameraDataObject(cameraDataElement.getAsJsonObject());
                    cameraDataList.add(innerList);
                }

                cameras.put(cameraKey, cameraDataList);
            }

            cameraDataWrapper.setCameras(cameras);
            return cameraDataWrapper;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<CameraData> parseCameraDataObject(JsonObject cameraDataObject) {
        List<CameraData> cameraDataList = new ArrayList<>();
        CameraData cameraData = new CameraData();
        cameraData.setTime(cameraDataObject.get("time").getAsInt());
        cameraData.setDetectedObjects(parseDetectedObjects(cameraDataObject.get("detectedObjects").getAsJsonArray()));
        cameraDataList.add(cameraData);
        return cameraDataList;
    }

    private static List<DetectedObject> parseDetectedObjects(JsonArray detectedObjectsArray) {
        List<DetectedObject> detectedObjects = new ArrayList<>();
        for (JsonElement element : detectedObjectsArray) {
            JsonObject detectedObject = element.getAsJsonObject();
            DetectedObject obj = new DetectedObject();
            obj.setId(detectedObject.get("id").getAsString());
            obj.setDescription(detectedObject.get("description").getAsString());
            detectedObjects.add(obj);
        }
        return detectedObjects;
    }

    public static synchronized List<Pose> PoseConfigParser(String filePath) {
        Gson gson = new Gson();

        // reading from the file
        try {
            FileReader reader = new FileReader(filePath);
            Type type = new TypeToken<List<Pose>>() {
            }.getType();

            List<Pose> poses = gson.fromJson(reader, type);

            return poses;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static synchronized List<Camera> initCameras(List<CameraConfiguration> cameraConfigurations,
            Map<String, List<List<CameraData>>> camerasData) {
        List<Camera> cameras = new ArrayList<>();
        for (CameraConfiguration cameraConfiguration : cameraConfigurations) {
            Camera camera = new Camera(cameraConfiguration.getId(), cameraConfiguration.getFrequency());// create an
                                                                                                        // instance of
                                                                                                        // camera with
                                                                                                        // the id and
                                                                                                        // frequency and
                                                                                                        // status by
                                                                                                        // default

            // create a list of StampedCloudPoints for each camera
            List<StampedDetectedObjects> stampedDetectedObjects = new ArrayList<>();
            for (List<CameraData> cameraDataList : camerasData.get(cameraConfiguration.getCamera_key())) {
                for (CameraData cameraData : cameraDataList) {
                    StampedDetectedObjects stampedDetectedObject = new StampedDetectedObjects(cameraData.getTime(),
                            cameraData.getDetectedObjects());
                    stampedDetectedObjects.add(stampedDetectedObject);
                }
            }

            camera.setStampedDetectedObjects(stampedDetectedObjects);
            cameras.add(camera);
        }

        return cameras;
    }

    public static synchronized List<LiDarWorkerTracker> initLiDars(List<LidarConfiguration> lidarConfigurations) {
        List<LiDarWorkerTracker> lidars = new ArrayList<>();
        for (LidarConfiguration lidarConfiguration : lidarConfigurations) {
            LiDarWorkerTracker lidar = new LiDarWorkerTracker(lidarConfiguration.getId(),
                    lidarConfiguration.getFrequency());// create an instance of lidar with the id and frequency and
                                                       // status by default

            lidars.add(lidar);
        }

        return lidars;
    }

    public static synchronized List<StampedCloudPoints> initCloudPoints(List<LidarData> lidarDatas) {

        List<StampedCloudPoints> stampedCloudPoints = new ArrayList<>();
        for (LidarData ld : lidarDatas) {
            StampedCloudPoints stampedCloudPoint = new StampedCloudPoints(ld.getId(), ld.getTime(),
                    ld.getCloudPoints());
            stampedCloudPoints.add(stampedCloudPoint);
        }

        return stampedCloudPoints;
    }

    public void createOutputFile(String outputPath, StatisticalFolder stats, List<Landmark> landmarks) {
        JsonObject output = new JsonObject();
        outputPath = path + outputPath;

        // Add statistics
        output.addProperty("systemRuntime", stats.getSystemRuntime());
        output.addProperty("numDetectedObjects", stats.getNumDetectedObjects());
        output.addProperty("numTrackedObjects", stats.getNumTrackedObjects());
        output.addProperty("numLandmarks", stats.getNumLandmarks());

        // Add world map
        JsonObject landMarksObj = new JsonObject();
        for (Landmark landmark : landmarks) {
            JsonObject landmarkObj = new JsonObject();
            landmarkObj.addProperty("id", landmark.getId());
            landmarkObj.addProperty("description", landmark.getDescription());

            JsonArray coordinatesArr = new JsonArray();
            for (CloudPoint point : landmark.getCoordinates()) {
                JsonObject coordObj = new JsonObject();
                coordObj.addProperty("x", point.getX());
                coordObj.addProperty("y", point.getY());
                coordinatesArr.add(coordObj);
            }
            landmarkObj.add("coordinates", coordinatesArr);
            landMarksObj.add(landmark.getId(), landmarkObj);
        }
        output.add("landMarks", landMarksObj);

        try (FileWriter writer = new FileWriter(outputPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(output, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createErrorOutputFile(String outputPath, StatisticalFolder stats, List<Landmark> landmarks,
            Object crasher, List<Camera> cameras, List<LiDarWorkerTracker> lidars) {
        JsonObject output = new JsonObject();
        outputPath = path + outputPath;

        if (crasher instanceof Camera) {
            Camera camera = (Camera) crasher;
            output.addProperty("error", camera.getErrorDescription());
            output.addProperty("faultySensor", "Camera" + camera.getId());
        } else if (crasher instanceof LiDarWorkerTracker) {
            LiDarWorkerTracker lidar = (LiDarWorkerTracker) crasher;
            output.addProperty("error", "Connection to LiDAR lost");
            output.addProperty("faultySensor", "LiDAR" + lidar.getId());
        }

        // Add last cameras frame
        JsonObject camerasFrameObj = new JsonObject();
        for (Camera camera : cameras) {
            JsonObject frameObj = new JsonObject();
            frameObj.addProperty("time", camera.getLastDetectedObjects().getTime());
            JsonArray detectedObjectsArr = new JsonArray();
            for (DetectedObject obj : camera.getLastDetectedObjects().getDetectedObjects()) {
                JsonObject objObj = new JsonObject();
                objObj.addProperty("id", obj.getId());
                objObj.addProperty("description", obj.getDescription());
                detectedObjectsArr.add(objObj);
            }
            frameObj.add("detectedObjects", detectedObjectsArr);
            camerasFrameObj.add("Camera" + camera.getId(), frameObj);
        }
        output.add("lastCamerasFrame", camerasFrameObj);

        // Add last LiDar worker trackers frame
        JsonObject lidarFrameObj = new JsonObject();
        for (LiDarWorkerTracker lidar : lidars) {
            JsonArray trackedObjectsArr = new JsonArray();
            for (TrackedObject obj : lidar.getLastTrackedObject()) {
                JsonObject objObj = new JsonObject();
                objObj.addProperty("id", obj.getId());
                objObj.addProperty("time", obj.getTime());
                objObj.addProperty("description", obj.getDescription());
                JsonArray coordinatesArr = new JsonArray();
                for (CloudPoint point : obj.getCoordinates()) {
                    JsonObject coordObj = new JsonObject();
                    coordObj.addProperty("x", point.getX());
                    coordObj.addProperty("y", point.getY());
                    coordinatesArr.add(coordObj);
                }
                objObj.add("coordinates", coordinatesArr);
                trackedObjectsArr.add(objObj);
            }
            lidarFrameObj.add("LiDarWorkerTracker" + lidar.getId(), trackedObjectsArr);
        }
        output.add("lastLiDarWorkerTrackersFrame", lidarFrameObj);

        // Add poses
        JsonArray posesArr = new JsonArray();
        for (Pose pose : FusionSlam.getInstance().getPoses()) {
            if (pose != null) {
                JsonObject poseObj = new JsonObject();
                poseObj.addProperty("time", pose.getTime());
                poseObj.addProperty("x", pose.getX());
                poseObj.addProperty("y", pose.getY());
                poseObj.addProperty("yaw", pose.getYaw());
                posesArr.add(poseObj);
            }
        }
        output.add("poses", posesArr);

        // Add statistics
        JsonObject statsObj = new JsonObject();
        statsObj.addProperty("systemRuntime", stats.getSystemRuntime());
        statsObj.addProperty("numDetectedObjects", stats.getNumDetectedObjects());
        statsObj.addProperty("numTrackedObjects", stats.getNumTrackedObjects());
        statsObj.addProperty("numLandmarks", stats.getNumLandmarks());

        // Add landmarks
        JsonObject landMarksObj = new JsonObject();
        for (Landmark landmark : landmarks) {
            JsonObject landmarkObj = new JsonObject();
            landmarkObj.addProperty("id", landmark.getId());
            landmarkObj.addProperty("description", landmark.getDescription());
            JsonArray coordinatesArr = new JsonArray();
            for (CloudPoint point : landmark.getCoordinates()) {
                JsonObject coordObj = new JsonObject();
                coordObj.addProperty("x", point.getX());
                coordObj.addProperty("y", point.getY());
                coordinatesArr.add(coordObj);
            }
            landmarkObj.add("coordinates", coordinatesArr);
            landMarksObj.add(landmark.getId(), landmarkObj);
        }
        statsObj.add("landMarks", landMarksObj);
        output.add("statistics", statsObj);

        // Write to file
        try (FileWriter writer = new FileWriter(outputPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(output, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}