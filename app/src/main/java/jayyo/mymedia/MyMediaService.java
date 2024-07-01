package jayyo.mymedia;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.io.IOException;

public class MyMediaService extends Service {
    private final static String TAG = "MyMediaService";
    private static Intent resultIntent;

    private MediaRecorder mediaRecorder;
    private MediaProjection mediaProjection;
    private WindowManager windowManager;
    private int screenDensity;
    private int screenWidth;
    private int screenHeight;
    private VirtualDisplay virtualDisplay;
    private Button igv;
    private String filePath;
    private Button igv2;

    public static void setResultIntent(Intent it) {
        resultIntent = it;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initWindow();
        initMediaProjection();
    }

    private void initWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        getScreenParameter();

        Log.d(TAG, "density:" + screenDensity + ", width:" + screenWidth + ", height:" + screenHeight);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = screenHeight / 4;

        igv = new Button(this);
        igv.setText("開始錄影");
        igv.setOnClickListener(v -> startRecording());
        igv.setOnLongClickListener(v -> {
            stopSelf();
            return false;
        });

        windowManager.addView(igv, params);

        //沒做成功
//        params.gravity = Gravity.TOP | Gravity.END;
//
//        igv2 = new Button(this);
//        igv2.setText("結束錄影");
//        igv2.setOnClickListener(v -> {
//            stopRecord();
//            startMediaPlayer();
//        });
//        windowManager.addView(igv2, params);
    }

    private void startMediaPlayer() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            Log.i(TAG, "path:" + filePath);
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepareAsync();
            Log.i(TAG, "prepareAsync");
        } catch (IOException e) {
            Log.e(TAG, "IOException:" + e);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        Log.i(TAG, "play Media");
    }

    private void getScreenParameter() {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenDensity = metrics.densityDpi;
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
    }

    private void initMediaProjection() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, resultIntent);
    }

    private void createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenRecording", screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(), null, null);
    }

    private void setUpMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(getScreenRecordingFilePath());
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(screenWidth, screenHeight);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "IOException:" + e);
        }
    }

    public String getScreenRecordingFilePath() {
        String tmp = "sdcard/Download/MyMedia_" + System.currentTimeMillis() + ".mp4";
        Log.i(TAG, "path:" + tmp);
        filePath = tmp;
        return tmp;
    }

    public void startRecording() {
        getScreenParameter();
//        igv.setVisibility(View.GONE);
        setUpMediaRecorder();
        createVirtualDisplay();
        Log.i(TAG, "start");
        mediaRecorder.start();
    }

    public void stopRecord() {
        mediaRecorder.stop();
//        mediaRecorder.reset();
        virtualDisplay.release();
//        igv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        stopRecord();

        if (igv != null) windowManager.removeView(igv);
        if (igv2 != null) windowManager.removeView(igv2);

        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
    }
}
