import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class EuclideanDistTracker {
    private Map<Integer, Point> centerPoints;
    private Map<Integer, Point> previousCenterPoints;
    private Map<Integer, Double> speeds;
    private int idCount;
    private static final double FPS = 30.0;
    private static final double PIXELS_TO_METERS = 0.05;

    public EuclideanDistTracker() {
        centerPoints = new HashMap<>();
        previousCenterPoints = new HashMap<>();
        speeds = new HashMap<>();
        idCount = 0;
    }

    public List<int[]> update(List<Rect> objectsRect, Mat frame) {
        List<int[]> objectsBbsIds = new ArrayList<>();

        for (Rect rect : objectsRect) {
            int x = rect.x;
            int y = rect.y;
            int w = rect.width;
            int h = rect.height;
            int cx = (x + x + w) / 2;
            int cy = (y + y + h) / 2;

            boolean sameObjectDetected = false;
            for (Map.Entry<Integer, Point> entry : centerPoints.entrySet()) {
                int id = entry.getKey();
                Point pt = entry.getValue();
                double dist = Math.hypot(cx - pt.x, cy - pt.y);

                if (dist < 25) {
                    double speed = calculateSpeed(id, cx, cy);
                    previousCenterPoints.put(id, centerPoints.get(id));
                    centerPoints.put(id, new Point(cx, cy));
                    speeds.put(id, speed);
                    objectsBbsIds.add(new int[]{x, y, w, h, id, (int) speed});
                    sameObjectDetected = true;
                    break;
                }
            }

            if (!sameObjectDetected) {
                centerPoints.put(idCount, new Point(cx, cy));
                previousCenterPoints.put(idCount, new Point(cx, cy));
                speeds.put(idCount, 0.0);
                objectsBbsIds.add(new int[]{x, y, w, h, idCount, 0});
                idCount++;
            }
        }

        Map<Integer, Point> newCenterPoints = new HashMap<>();
        for (int[] objBbId : objectsBbsIds) {
            int objectId = objBbId[4];
            Point center = centerPoints.get(objectId);
            newCenterPoints.put(objectId, center);
        }

        centerPoints = newCenterPoints;
        return objectsBbsIds;
    }

    private double calculateSpeed(int id, int cx, int cy) {
        Point prevCenter = previousCenterPoints.get(id);
        if (prevCenter == null) {
            return 0.0;
        }

        double distance = Math.hypot(cx - prevCenter.x, cy - prevCenter.y) * PIXELS_TO_METERS;
        double speed = (distance * FPS) * 3.6;
        return speed;
    }
}
