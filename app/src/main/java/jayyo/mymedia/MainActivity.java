package jayyo.mymedia;

import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int SCREEN_CAPTURE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MediaProjectionManager mediaPM = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaPM.createScreenCaptureIntent();
        startActivityForResult(intent, SCREEN_CAPTURE_PERMISSION_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("MainActivity", "requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == SCREEN_CAPTURE_PERMISSION_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle resultData = new Bundle();
                resultData.putParcelable("KEY_SCREEN_CAPTURE_INTENT", data);
                Log.i("MainActivity", "ScreenCapture result:" + resultData);

                MyMediaService.setResultIntent(data);
                startService(new Intent(getApplicationContext(), MyMediaService.class));
            } else {
                Log.e("MainActivity", "ScreenCapture permission denied");
            }
        }
    }
}