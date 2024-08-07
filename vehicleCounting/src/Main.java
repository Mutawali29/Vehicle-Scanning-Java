import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        EuclideanDistTracker tracker = new EuclideanDistTracker();

        VideoCapture cap = new VideoCapture("src/highway.mp4");

        BackgroundSubtractorMOG2 objectDetector = Video.createBackgroundSubtractorMOG2(100, 40, false);

        Mat frame = new Mat();
        Mat roi = new Mat();
        Mat mask = new Mat();
        List<Rect> detections = new ArrayList<>();

        while (true) {
            cap.read(frame);
            if (frame.empty()) break;

            int height = frame.rows();
            int width = frame.cols();

            roi = frame.submat(340, 720, 500, 800);

            objectDetector.apply(roi, mask);
            Imgproc.threshold(mask, mask, 254, 255, Imgproc.THRESH_BINARY);

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(mask, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            detections.clear();
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > 100) {
                    Rect bbox = Imgproc.boundingRect(contour);
                    detections.add(bbox);
                }
            }

            List<int[]> boxesIds = tracker.update(detections, roi);
            for (int[] boxId : boxesIds) {
                int x = boxId[0];
                int y = boxId[1];
                int w = boxId[2];
                int h = boxId[3];
                int id = boxId[4];
                double speed = boxId[5];
                String vehicleType = boxId.length > 6 && boxId[6] == 0 ? "Car" : "Motorcycle";

                Imgproc.putText(roi, Integer.toString(id), new Point(x, y - 15), Imgproc.FONT_HERSHEY_PLAIN, 1, new Scalar(255, 0, 0), 2);
                Imgproc.rectangle(roi, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 255, 0), 3);
                Imgproc.putText(roi, String.format("%.2f km/h", speed), new Point(x, y - 35), Imgproc.FONT_HERSHEY_PLAIN, 1, new Scalar(0, 255, 255), 2);
                Imgproc.putText(roi, vehicleType, new Point(x, y - 55), Imgproc.FONT_HERSHEY_PLAIN, 1, new Scalar(0, 255, 255), 2);
            }

            HighGui.imshow("roi", roi);
            HighGui.imshow("Frame", frame);
            HighGui.imshow("Mask", mask);

            int key = HighGui.waitKey(30);
            if (key == 27) break;
        }

        cap.release();
        HighGui.destroyAllWindows();
    }
}

