package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

class FusionSlamTest {
    /*
     * the function receives a list of TrackedObjects and transform them to Landmarks
     *
     * @param trackedObjects - the list of TrackedObjects to process
     *
     * @INVARIENTS:
     * 1. the field landmarks has only valid Landmark objects.
     * -the landmarks correctly represent the environment and initialized correctly.
     * -the landmars are not null.
     * -the field landmarks is not null.
     * 2. the function is Thread safe.
     *
     * @PRE-CONDITIONS:
     * 1. The list of TrackedObjects is not null.
     *
     * @POST-CONDITIONS:
     * 1. The function will add new Landmarks to the map if the TrackedObject is new, else it will update the existing Landmark
     */
    private FusionSlam fusionSlam;
    private List<TrackedObject> trackedObjects;

    // Set up method to initialize the required fields
    @BeforeEach
    void setUp() {
        fusionSlam = FusionSlam.getInstance();

        // Initialize landmarks
        fusionSlam.setLandmarks(new ArrayList<>());

        // Create some tracked objects for testing
        trackedObjects = new ArrayList<>();

        // Example of TrackedObject with ID, description, and coordinates
        List<CloudPoint> points = new ArrayList<>();
        points.add(new CloudPoint(1, 1));
        points.add(new CloudPoint(2, 2));
        points.add(new CloudPoint(3, 3));
        TrackedObject trackedObject1 = new TrackedObject("1", 1, "first tracked object", points);
        points.clear();
        points.add(new CloudPoint(4, 4));
        points.add(new CloudPoint(5, 5));
        points.add(new CloudPoint(6, 6));
        points.add(new CloudPoint(7, 7));
        TrackedObject trackedObject2 = new TrackedObject("2", 2, "second tracked object", new ArrayList<>());
        // Add tracked objects to the list
        trackedObjects.add(trackedObject1);
        trackedObjects.add(trackedObject2);
        // Add poses
        fusionSlam.updatePose(new Pose(0, 0, 30, 0));
        fusionSlam.updatePose(new Pose(1, 1, 35, 1));
        fusionSlam.updatePose(new Pose(2, 2, 40, 2));
    }

    // Test for adding new landmarks
    @Test
    void testProcessTrackedObjects_AddNewLandmarks() {
        // Before processing
        assertTrue(fusionSlam.getLandmarks().isEmpty(), "Landmarks should be empty initially");

        // Process the tracked objects
        fusionSlam.processTrackedObjects(trackedObjects,0);

        // After processing, check that landmarks are added
        assertEquals(2, fusionSlam.getLandmarks().size(), "There should be 2 landmarks after processing");

        Landmark landmark1 = fusionSlam.getLandmarks().get(0);
        Landmark landmark2 = fusionSlam.getLandmarks().get(1);

        assertNotNull(landmark1, "Landmark1 should not be null");
        assertNotNull(landmark2, "Landmark2 should not be null");
        assertEquals("1", landmark1.getId(), "Landmark1 ID should match tracked object ID");
        assertEquals("2", landmark2.getId(), "Landmark2 ID should match tracked object ID");
    }

    // Test for updating existing landmarks
    @Test
    void testProcessTrackedObjects_UpdateExistingLandmarks() {
        // Add initial landmarks
        Landmark existingLandmark = new Landmark("1", "object1", new ArrayList<>());
        fusionSlam.getLandmarks().add(existingLandmark);

        // Process the tracked objects with the same ID as the existing landmark
        fusionSlam.processTrackedObjects(trackedObjects,1);

        // After processing, check that only one landmark exists and it was updated
        assertEquals(2, fusionSlam.getLandmarks().size(), "There should be 2 landmarks after processing");

        Landmark updatedLandmark = fusionSlam.getLandmarks().get(0);  // "1" landmark should be updated
        assertEquals("1", updatedLandmark.getId(), "Landmark ID should remain the same after update");
        // You can add further checks to confirm the updated coordinates after averaging
    }

    // Test for thread safety (this tests concurrent access)
    @Test
    void testProcessTrackedObjects_ThreadSafety() throws InterruptedException {
        // Start two threads to process the same tracked objects concurrently
        Thread thread1 = new Thread(() -> fusionSlam.processTrackedObjects(trackedObjects,1));
        Thread thread2 = new Thread(() -> fusionSlam.processTrackedObjects(trackedObjects,2));

        // Start threads
        thread1.start();
        thread2.start();

        // Join threads to make sure both finish execution
        thread1.join();
        thread2.join();

        // After both threads have processed, check the number of landmarks
        assertEquals(2, fusionSlam.getLandmarks().size(), "The number of landmarks should be consistent after concurrent access");
    }

    // Test to ensure statistical update when new landmarks are added
    @Test
    void testProcessTrackedObjects_StatisticalUpdate() {
        // Assuming StatisticalFolder is a singleton class with an incrementLandmarks() method
        StatisticalFolder statisticalFolder = StatisticalFolder.getInstance();
        int initialLandmarkCount = statisticalFolder.getNumLandmarks();  // hypothetical method to get the count

        // Process tracked objects
        fusionSlam.processTrackedObjects(trackedObjects,1);

        // Check if the landmark count is updated correctly
        assertEquals(initialLandmarkCount + 2, statisticalFolder.getNumLandmarks(), "Landmark count should increment by 2 after adding new landmarks");
    }

    // Test when input trackedObjects list is empty
    @Test
    void testProcessTrackedObjects_EmptyTrackedObjects() {
        List<TrackedObject> emptyTrackedObjects = new ArrayList<>();

        // Process empty list
        fusionSlam.processTrackedObjects(emptyTrackedObjects,1);

        // Check that no landmarks are added
        assertEquals(0, fusionSlam.getLandmarks().size(), "Landmarks should remain empty when no tracked objects are provided");
    }
}