package gershon.wolframvoice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class PermissionRequestUtil {

    public static void requestAudioPermission(MainActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {

            } else {
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(activity,
                            "App required access to audio", Toast.LENGTH_SHORT).show();
                }
                activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO
                }, 101);
            }

        } else {

        }
    }
}
