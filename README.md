# vehicle-scanning-java
The program uses OpenCV to detect and track objects in a video. Frames are read from the video input using `VideoCapture`. The ROI is analyzed with background subtraction to detect objects. `EuclideanDistTracker` tracks objects based on Euclidean distance and calculates their speed. Detection and tracking results are displayed in a GUI window.
