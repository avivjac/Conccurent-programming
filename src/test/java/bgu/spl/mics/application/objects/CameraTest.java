package bgu.spl.mics.application.objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
     * @INVARIANT:
     * 1. the field this.stampedDetectedObjects is not null.
     * 2. all the objects in this.stampedDetectedObjects are valid.
     * 
     * @PRE-CONDITION:
     * 1. the input time must be a valid integer representing a meaningful timestamp.
     * 
     * @POST-CONDITION:
     * 1. the field this.stampedDetectedObjects isn't changed.
     * 2. The method always returns a non-null List<DetectedObject>.
     */
public class CameraTest {
    private Camera camera;
    private List<StampedDetectedObjects> stampedDetectedObjectsList;

    @BeforeEach
    public void setUp() {
        stampedDetectedObjectsList = new ArrayList<>();
        stampedDetectedObjectsList.add(new StampedDetectedObjects(1, Arrays.asList(
                new DetectedObject("obj1", "description1"),
                new DetectedObject("obj2", "description2")
        )));
        stampedDetectedObjectsList.add(new StampedDetectedObjects(2, Arrays.asList(
                new DetectedObject("obj3", "description3"),
                new DetectedObject("obj4", "description4")
        )));
        camera = new Camera(1, 10, STATUS.UP, stampedDetectedObjectsList);
    }

    @Test
    public void testGetDetectedObjectsByTime() {
        // Precondition: Valid time
        int time = 1;

        // Call the method
        List<DetectedObject> detectedObjects = camera.GetDetectedObjectsByTime(time);

        System.out.println("first test");
        // Postconditions
        assertNotNull(detectedObjects);
        assertEquals(2, detectedObjects.size());
        assertEquals("obj1", detectedObjects.get(0).getId());
        assertEquals("obj2", detectedObjects.get(1).getId());

        // Invariants
        assertNotNull(camera.getStampedDetectedObjects());
        for (StampedDetectedObjects stampedObject : camera.getStampedDetectedObjects()) {
            assertNotNull(stampedObject);
            assertNotNull(stampedObject.getDetectedObjects());
        }
    }

    @Test
    public void testGetDetectedObjectsByTimeNoMatch() {
        // Precondition: Valid time with no matching objects
        int time = 3;

        // Call the method
        List<DetectedObject> detectedObjects = camera.GetDetectedObjectsByTime(time);

        System.out.println("second test");
        // Postconditions
        assertNotNull(detectedObjects);
        assertTrue(detectedObjects.isEmpty());

        // Invariants
        assertNotNull(camera.getStampedDetectedObjects());
        for (StampedDetectedObjects stampedObject : camera.getStampedDetectedObjects()) {
            assertNotNull(stampedObject);
            assertNotNull(stampedObject.getDetectedObjects());
        }
    }
}
