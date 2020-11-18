# Read image from file:
var img = Imgcodecs.imread(imagePath);

# Crop image:
var roi = new Rect(207, 15, 728, 926);
var cropped = new Mat(img, roi);

# Write image to file:
Imgcodecs.imwrite("/home/knovak/Pictures/opencv/cropped.jpg", cropped);

# Byte array to Mat:
var mat = Imgcodecs.imdecode(new MatOfByte(photo), Imgcodecs.IMREAD_COLOR);

