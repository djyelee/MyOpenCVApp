package edu.byu.rvl.myopencvapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MyOpenCVActivity extends Activity implements View.OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG = "Sample::MyOpenCV::Activity";
    private CameraBridgeViewBase mOpenCvCameraView;

    public static final int      	VIEW_MODE_RGBA      = 0;
    public static final int      	VIEW_MODE_DIFF      = 1;

    private MenuItem             	mItemPreviewRGBA;
    private MenuItem             	mItemPreviewDIFF;

    public static int           	viewMode = VIEW_MODE_RGBA;
    static final int 				N_BUFFERS = 2;
    static final int				NUM_FINGERS = 1;

    public static float[]			TouchX, TouchY;
    public static float				StartX, StartY;
    public static int				actionCode;
    public static int				pointerCount = 0;
    public static int				inputValue = 0;

    Mat mRgba[];
    Mat mDisplay;
    String Msg;
    int	bufferIndex;
    int FrameHeight;
    int FrameWidth;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MyOpenCVActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "Creating and setting view");
        mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(this, -1);
        setContentView(mOpenCvCameraView);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        int i;
        mRgba = new Mat[N_BUFFERS];
        TouchX = new float[NUM_FINGERS];
        TouchY = new float[NUM_FINGERS];
        inputValue = 0;
        bufferIndex = 0;
        FrameHeight = height;
        FrameWidth = width;
        for (i=0; i<N_BUFFERS; i++) {
            mRgba[i]= new Mat(height, width, CvType.CV_8UC4);
        }
        mDisplay= new Mat(height, width, CvType.CV_8UC3);
    }

    public void onCameraViewStopped() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_open_cv, menu);

        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA  = menu.add("RGB");
        mItemPreviewDIFF  = menu.add("Difference");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA) {
            viewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewDIFF) {
            viewMode = VIEW_MODE_DIFF;
            mDisplay= new Mat(FrameHeight, FrameWidth, CvType.CV_8UC4);
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onTouch(View v, MotionEvent event) {
        int i;
        pointerCount = event.getPointerCount();
        actionCode = event.getAction();
        if (actionCode == MotionEvent.ACTION_DOWN) {								// get the starting location from the first touch
            StartX = event.getX(0);
            StartY = event.getY(0);
        } else if (actionCode == MotionEvent.ACTION_MOVE) {
            for(i=0; i<pointerCount && i<NUM_FINGERS; i++) {						// get locations for up to to 5 touches
                TouchX[i] = event.getX(i);
                TouchY[i] = event.getY(i);

            }
        } else if (actionCode == MotionEvent.ACTION_UP && pointerCount > 0) {		// update the distance
            inputValue = (int)(TouchX[0] - StartX);
        }
        return true;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        switch (viewMode) {
            case VIEW_MODE_RGBA:
                mDisplay = inputFrame.rgba();
                break;
            case VIEW_MODE_DIFF:
                mRgba[bufferIndex] = inputFrame.rgba();
                Core.absdiff(mRgba[bufferIndex], mRgba[1-bufferIndex], mDisplay);
                bufferIndex = 1 - bufferIndex;
                Msg = (" " + inputValue);
                // Use Core.putText in 2.4.10   Imgproc.putText in 3.1.0 but it doesn't work
                // Imgproc.putText(mDisplay, Msg, new Point(10, 100), Core.FONT_HERSHEY_COMPLEX, 2.0, new Scalar(255, 0, 0, 255));
                break;
        }
        return mDisplay;
    }

}
