package com.quip.opencvanduvc;

import android.graphics.Bitmap;
import android.view.PixelCopy;
import android.view.SurfaceView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageProcess {
    int zebraprecentage = 200;
    boolean focuscolor = false;
    boolean histypecolor = false;
    boolean settings_call = true;
    boolean ismenu_show = false;
    int screen_height, screen_width;
    int temp=2, tint =5;
    int sharpness = 1;
    int brightness=50,contrast=50,saturation=50;

    public Bitmap drawBitmap(SurfaceView mResultView) {

        Bitmap surfaceBitmap = Bitmap.createBitmap(mResultView.getWidth(), mResultView.getHeight(), Bitmap.Config.ARGB_8888);
        PixelCopy.OnPixelCopyFinishedListener listener = copyResult -> {
        };
        PixelCopy.request(mResultView, surfaceBitmap, listener, mResultView.getHandler());

//        Mat imageMat = new Mat();
//        Mat imageDest = new Mat();
//
//        Utils.bitmapToMat(surfaceBitmap, imageMat);
//
//        Imgproc.resize(imageMat, imageDest, new Size(480, 270), .5, .5, Imgproc.INTER_AREA);
//        surfaceBitmap = Bitmap.createBitmap(imageDest.cols(), imageDest.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(imageDest, surfaceBitmap);
//
//        imageMat.release();
//        imageDest.release();
//
//        imageMat = new Mat();
//        imageDest = new Mat();
//
//        Utils.bitmapToMat(surfaceBitmap,imageMat);
//
//        Imgproc.resize(imageMat, imageDest, new Size(Math.round(aspect_width), Math.round(aspect_height)), 1, 1, Imgproc.INTER_AREA);
//        surfaceBitmap = Bitmap.createBitmap(imageDest.cols(), imageDest.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(imageDest, surfaceBitmap);

        return surfaceBitmap;
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    Mat reduceColors(Mat img, int numRed, int numGreen, int numBlue) {
        Mat redLUT = createLUT(numRed);
        Mat greenLUT = createLUT(numGreen);
        Mat blueLUT = createLUT(numBlue);


        List<Mat> BGR = new ArrayList<>(4);
        Core.split(img, BGR); // splits the image into its channels in the List of Mat arrays.

        Core.LUT(BGR.get(0), blueLUT, BGR.get(0));
        Core.LUT(BGR.get(1), greenLUT, BGR.get(1));
        Core.LUT(BGR.get(2), redLUT, BGR.get(2));
        Core.merge(BGR, img);

        return img;
    }

    Mat reduceColors(Mat img, int numRed, int numGreen, int numBlue, int numalpha) {

        Mat alpha = new Mat(img.height(), img.width(), img.type());
        Mat dst = new Mat();


        Scalar scalar = new Scalar(numRed, numGreen, numBlue, numalpha);
        Mat mat = alpha.setTo(scalar);

//Core.multiply(mat,img,dst);
        return mat;
    }

    Mat createLUT(int numColors) {
        // When numColors=1 the LUT will only have 1 color which is black.
        if (numColors < 0 || numColors > 256) {
            System.out.println("Invalid Number of Colors. It must be between 0 and 256 inclusive.");
            return null;
        }

        Mat lookupTable = Mat.zeros(new Size(1, 256), CvType.CV_8UC1);

        int startIdx = 0;
        for (int x = 0; x < 256; x += 256.0 / numColors) {
            lookupTable.put(x, 0, x);

            for (int y = startIdx; y < x; y++) {
                if (lookupTable.get(y, 0)[0] == 0) {
                    lookupTable.put(y, 0, lookupTable.get(x, 0));
                }
            }
            startIdx = x;
        }
        return lookupTable;
    }


    //FOCUS
    int scale = 5;
    public ArrayList<Bitmap> edgesimg(Bitmap bitmap) {
        Bitmap original = bitmap;
        bitmap = getResizedBitmap(bitmap, 700);
        ArrayList<Bitmap> ba = new ArrayList<>();
        try {
            Mat src = new Mat();
            Mat src_gray = new Mat();
            Mat dst = new Mat();
            Mat abs_dst = new Mat();
            Mat result = new Mat();
            Utils.bitmapToMat(bitmap, src);
            src.convertTo(src_gray, CvType.CV_64F);
//            Size GaussianBlur_kernel_size = new Size(3, 3);
//            Imgproc.GaussianBlur(src, src, GaussianBlur_kernel_size, 3, 3, Core.BORDER_DEFAULT);
            Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_RGB2GRAY);

            Imgproc.Laplacian(src_gray, dst, CvType.CV_8U, 1, scale, -200, Core.BORDER_DEFAULT);

            //Imgproc.Canny(src_gray,dst,255,255,3);
            Core.convertScaleAbs(dst, abs_dst);
            Bitmap resultBitmap = Bitmap.createBitmap(abs_dst.cols(), abs_dst.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(abs_dst, resultBitmap);
            Bitmap overlay = makeBlackTransparent(resultBitmap);
            Utils.bitmapToMat(overlay, result);
            Mat mat;
            if (focuscolor) {
                mat = reduceColors(result, 0, 0, 50);
            } else {
                mat = reduceColors(result, 0, 50, 0);
            }
            Utils.matToBitmap(mat, overlay);
            ba.add(original);
            ba.add(resultBitmap);
            ba.add(overlay);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return ba;
    }
    private Bitmap makeBlackTransparent(Bitmap image) {
        // convert image to matrix
        Mat src = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, src);

        // init new matrices
        Mat dst = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat tmp = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat alpha = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);

        // convert image to grayscale
        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGR2GRAY);

        // threshold the image to create alpha channel with complete transparency in black background region and zero transparency in foreground object region.
        Imgproc.threshold(tmp, alpha, 100, 255, Imgproc.THRESH_BINARY);

        // split the original image into three single channel.
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);

        // Create the final result by merging three single channel and alpha(BGRA order)
        List<Mat> rgba = new ArrayList<Mat>(4);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);
        Core.merge(rgba, dst);

        // convert matrix to output bitmap
        Bitmap output = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dst, output);
        return output;
    }

    //ZEBRA
    private Bitmap makeWhiteTransparent(Bitmap image) {
        // convert image to matrix
        Mat src = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, src);
        // init new matrices
        Mat dst = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat tmp = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        Mat alpha = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8UC4);
        // convert image to grayscale
        Imgproc.cvtColor(src, tmp, Imgproc.COLOR_BGR2GRAY);
        // threshold the image to create alpha channel with complete transparency in white background region and zero transparency in foreground object region.
        Imgproc.threshold(tmp, alpha, zebraprecentage, 255, Imgproc.THRESH_BINARY_INV);
        // split the original image into three single channel.
        List<Mat> rgb = new ArrayList<Mat>(3);
        Core.split(src, rgb);
        // Create the final result by merging three single channel and alpha(BGRA order)

        List<Mat> rgba = new ArrayList<Mat>(4);
        rgba.add(rgb.get(0));
        rgba.add(rgb.get(1));
        rgba.add(rgb.get(2));
        rgba.add(alpha);
        Core.merge(rgba, dst);
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGBA2mRGBA);
        // convert matrix to output bitmap

        Utils.matToBitmap(dst, image);
        //Bitmap bitmap = removeBackground(output);
        return image;
    }
    public Bitmap zebra(Bitmap original) {
        Bitmap zebraBitmap = makeWhiteTransparent(original);
        return zebraBitmap;
    }

    //HISTOGRAM
    int histW = 2048, histH = 550;
    public Bitmap Histogram(Bitmap bitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(src, bgrPlanes);
        int histSize = 256;
        float[] range = {0, 256}; //the upper boundary is exclusive
        MatOfFloat histRange = new MatOfFloat(range);
        boolean accumulate = false;
        Mat bHist = new Mat(), gHist = new Mat(), rHist = new Mat();
        Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), bHist, new MatOfInt(histSize), histRange, accumulate);
        Imgproc.calcHist(bgrPlanes, new MatOfInt(1), new Mat(), gHist, new MatOfInt(histSize), histRange, accumulate);
        Imgproc.calcHist(bgrPlanes, new MatOfInt(2), new Mat(), rHist, new MatOfInt(histSize), histRange, accumulate);

        int binW = (int) Math.round((double) histW / histSize);
        Mat histImage = new Mat(histH, histW, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Core.normalize(bHist, bHist, 0, histImage.rows(), Core.NORM_MINMAX);
        Core.normalize(gHist, gHist, 0, histImage.rows(), Core.NORM_MINMAX);
        Core.normalize(rHist, rHist, 0, histImage.rows(), Core.NORM_MINMAX);
        float[] bHistData = new float[(int) (bHist.total() * bHist.channels())];
        bHist.get(0, 0, bHistData);
        float[] gHistData = new float[(int) (gHist.total() * gHist.channels())];
        gHist.get(0, 0, gHistData);
        float[] rHistData = new float[(int) (rHist.total() * rHist.channels())];
        rHist.get(0, 0, rHistData);
        for (int i = 1; i < histSize; i++) {
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(bHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(bHistData[i])), new Scalar(255, 0, 0), 2);
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(gHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(gHistData[i])), new Scalar(0, 255, 0), 2);
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(rHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(rHistData[i])), new Scalar(0, 0, 255), 2);
        }


        Bitmap resultBitmap = Bitmap.createBitmap(histImage.cols(), histImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(histImage, resultBitmap);

        return resultBitmap;
    }
    public Bitmap StanderdHistogram(Bitmap bitmap) {

        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
        List<Mat> bgrPlanes = new ArrayList<>();
        Core.split(src, bgrPlanes);
        int histSize = 256;
        float[] range = {0, 256}; //the upper boundary is exclusive
        MatOfFloat histRange = new MatOfFloat(range);
        boolean accumulate = false;
        Mat bHist = new Mat();
        Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), bHist, new MatOfInt(histSize), histRange, accumulate);


        int binW = (int) Math.round((double) histW / histSize);
        Mat histImage = new Mat(histH, histW, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Core.normalize(bHist, bHist, 0, histImage.rows(), Core.NORM_MINMAX);
        float[] bHistData = new float[(int) (bHist.total() * bHist.channels())];
        bHist.get(0, 0, bHistData);
        for (int i = 1; i < histSize; i++) {
            Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(bHistData[i - 1])),
                    new Point(binW * (i), histH - Math.round(bHistData[i])), new Scalar(153, 153, 153), 2);
        }

        Bitmap resultBitmap = Bitmap.createBitmap(histImage.cols(), histImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(histImage, resultBitmap);

        return resultBitmap;
    }

//    //SHARPNESS
//    public void sharpness(int value) {
//        synchronized (mSync) {
//            if (mUVCCamera != null) {
//                System.out.println("before=" + mUVCCamera.getSharpness());
//                mUVCCamera.resetSharpness();
//                mUVCCamera.updateCameraParams();
//                mUVCCamera.setSharpness(value);
//                mUVCCamera.updateCameraParams();
//                int mSharpnessMax = mUVCCamera.mSharpnessMax;
//                System.out.println("after=" + mUVCCamera.getSharpness());
//                System.out.println("max=" + mSharpnessMax);
//            }
//        }
//    }
//    public Bitmap sharpness(Bitmap original) {
//
//        //  original = BitmapFactory.decodeResource(getResources(),R.drawable.cybertruck);
//        Mat src = new Mat();
//        Mat kernel = new Mat();
//        Utils.bitmapToMat(original, src);
//        Mat dest = new Mat(src.rows(), src.cols(), src.type());
////        Imgproc.GaussianBlur(src, dest, new Size(0,0), 10);
////        Core.addWeighted(src, 1.5, src, -0.5, sharpness, dest);
//        Bitmap resultBitmap = Bitmap.createBitmap(dest.cols(), dest.rows(), Bitmap.Config.ARGB_8888);
////        Utils.matToBitmap(dest,resultBitmap);
//        int kernel_size = 3 + 2 * (sharpness % 5);
//        Mat ones = Mat.ones(kernel_size, kernel_size, CvType.CV_32F);
//        Core.multiply(ones, new Scalar(1 / (double) (kernel_size * kernel_size)), kernel);
//
//        Imgproc.filter2D(src, dest, -1, kernel, new Point(-1, -1), 0.0, Core.BORDER_DEFAULT);
//        Utils.matToBitmap(dest, resultBitmap);
//        return resultBitmap;
//    }

    //TINT
    public Bitmap whitebalanceTint(Bitmap original) {
        Mat src = new Mat();
        Mat mat = new Mat();
        Utils.bitmapToMat(original, src);

        if (tint == 0) {

            mat = reduceColors(src, 0, 0, 0, 0);
        } else if (tint > 0) {
            int f = (tint);
            System.out.println("temp" + tint);
            System.out.println("f=" + f);
            System.out.println("" + tint + "," + 0 + "," + tint);
            mat = reduceColors(src, tint, 0, tint, f);
        } else {
            int f = (tint * -1);
            System.out.println("temp =" + tint);
            System.out.println("f=" + f);
            System.out.println("" + 0 + "," + tint * -1 + "," + 0);
            mat = reduceColors(src, 0, (tint * -1), 0, f);
        }

        Bitmap resultBitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);

        return resultBitmap;

    }

    //TEMP
    public Bitmap whitebalanceTemp(Bitmap original) {
        Mat src = new Mat();
        Mat mat = new Mat();
        Utils.bitmapToMat(original, src);

        if (temp == 0) {

            mat = reduceColors(src, 0, 0, 0, 0);
        } else if (temp > 0) {
            int f = (temp);
            System.out.println("temp" + temp);
            System.out.println("f=" + f);
            System.out.println("" + temp + "," + 0 + "," + temp);
            mat = reduceColors(src, temp, temp, 0, f);
        } else {
            int f = (temp * -1);
            System.out.println("temp =" + temp);
            System.out.println("f=" + f);
            System.out.println("" + 0 + "," + temp * -1 + "," + 0);
            mat = reduceColors(src, 0, 0, (temp * -1), f);
        }

        Bitmap resultBitmap = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, resultBitmap);

        return resultBitmap;
    }

//    //BRIGHTNESS
//    public void brightness(int value) {
//        synchronized (mSync) {
//            if (mUVCCamera != null) {
//                mUVCCamera.resetBrightness();
//                mUVCCamera.updateCameraParams();
//                mUVCCamera.setBrightness(value);
//                mUVCCamera.updateCameraParams();
//                brightnestext.setText(value + "%");
//                brightnessseekBar.setProgress(value);
//                brightness=value;
//            }
//        }
//    }
//
//    //CONTRAST
//    public void contrast(int value) {
//        synchronized (mSync) {
//            if (mUVCCamera != null) {
//                mUVCCamera.resetContrast();
//                mUVCCamera.updateCameraParams();
//                mUVCCamera.setContrast(value);
//                mUVCCamera.updateCameraParams();
//                contrasttext.setText(value + "%");
//                contrastseekBar.setProgress(value);
//                contrast=value;
//            }
//        }
//    }
//
//    //SATURATION
//    public void saturation(int value) {
//        synchronized (mSync) {
//            if (mUVCCamera != null) {
//                System.out.println("before=" + mUVCCamera.getSaturation());
//                mUVCCamera.resetSaturation();
//                mUVCCamera.updateCameraParams();
//                mUVCCamera.setSaturation(value);
//                mUVCCamera.updateCameraParams();
//                saturationtext.setText(value+ "%");
//                saturationseekBar.setProgress(value);
//                saturation=value;
//            }
//        }
//    }

}
