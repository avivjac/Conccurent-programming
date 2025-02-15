package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

    /*
     * the function gets a list of StampedCloudPoints and a tick and returns a list of StampedCloudPoints that their time is equal to the tick
     *
     * @param data - a list of StampedCloudPoints
     * @param tick - the time we want to get the StampedCloudPoints from
     * 
     * @return a list of StampedCloudPoints that their time is equal to the tick
     * 
     * @INVARIANT:
     * 1. the parameter data is not null.
     * 2. all the stampedCloudPoints in data are valid.
     * 3. the returned list must only contain StampedCloudPoints from the input list with getTime() == tick.
     * 
     * @PRE-CONDITION:
     * 1. the input tick must be a valid integer representing a meaningful timestamp.
     * 
     * @POST-CONDITION:
     * 1. the method always returns a non-null List<DetectedObject>.
     * 2. if no StampedCloudPoints match the given tick, the returned list will be empty.
     */
class LiDarWorkerTrackerTest {
    //fields
    private List<StampedCloudPoints> data;

    @BeforeEach
    public void setUp() {
        // Initialize the common fields for all tests
        data = new ArrayList<>();
        List<CloudPoint> points = new ArrayList<>();
        points.add(new CloudPoint(1, 1));
        points.add(new CloudPoint(2, 2));
        points.add(new CloudPoint(3, 3));
        data.add(new StampedCloudPoints("id1", 1, points));
        points.clear();
        points.add(new CloudPoint(4, 4));
        points.add(new CloudPoint(5, 5));
        data.add(new StampedCloudPoints("id2", 2, points));
        points.clear();
        points.add(new CloudPoint(6, 6));
        points.add(new CloudPoint(7, 7));
        points.add(new CloudPoint(8, 8));
        data.add(new StampedCloudPoints("id3", 1, points));

    }

    @Test
    public void testValidInputMatchingTick() {
        // Test with valid data where some elements match the given tick
        List<StampedCloudPoints> result = LiDarWorkerTracker.cloudpointByTick(data, 1);

        // Verify the result
        assertNotNull(result, "The returned list should not be null.");
        assertEquals(2, result.size(), "The result should contain exactly 2 elements.");
        assertEquals("id1", result.get(0).getId(), "The first matching element's ID should be 'id1'.");
        assertEquals("id3", result.get(1).getId(), "The second matching element's ID should be 'id3'.");
    }

    @Test
    public void testValidInputNoMatchingTick() {
        // Test with valid data but no elements match the given tick
        List<StampedCloudPoints> result = LiDarWorkerTracker.cloudpointByTick(data, 3);

        // Verify the result
        assertNotNull(result, "The returned list should not be null.");
        assertTrue(result.isEmpty(), "The result should be empty if no elements match the tick.");
    }

    @Test
    public void testEmptyInputList() {
        // Test with an empty list
        data.clear();
        List<StampedCloudPoints> result = LiDarWorkerTracker.cloudpointByTick(data, 1);

        // Verify the result
        assertNotNull(result, "The returned list should not be null.");
        assertTrue(result.isEmpty(), "The result should be empty for an empty input list.");
    }

    @Test
    public void testOrderPreservation() {
        // Test to ensure the output list preserves the order of matching elements
        List<StampedCloudPoints> result = LiDarWorkerTracker.cloudpointByTick(data, 1);

        // Verify the order
        assertEquals("id1", result.get(0).getId(), "The first matching element should retain its order.");
        assertEquals("id3", result.get(1).getId(), "The second matching element should retain its order.");
    }
}