package gershon.wolframvoice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;
import java.util.Random;

import gershon.wolframvoice.activity.YoutubeActivity;

public class Utils {
    public static final String WOLFRAM_BASE_URL = "http://api.wolframalpha.com/v2/query?";

    private static final String[] RESPONSES = {"Right away boss", "Certainly," +
            " checking now", "You got it", "Checking now", "Excellent question", "I'm pickle rick" ,
            "Reeeee", "Maybe you should Google it yourself"};

    public static TextToSpeech textToSpeech;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getResponse() {
        int random = new Random().nextInt(RESPONSES.length);
        return RESPONSES[random];
    }

    public static boolean executeCommand(Activity activity, String query) {
        if(query.length() >= 5 && query.substring(0, 3).equalsIgnoreCase("Say")) {
            Utils.textToSpeech.speak(query.substring(3), TextToSpeech.QUEUE_FLUSH, null, null);
            return true;
        }
        if(query.length() >= 5 && query.substring(0, 4).equalsIgnoreCase("Play")) {
            Intent intent = new Intent(activity, YoutubeActivity.class);
            intent.putExtra("video", query.substring(4));
            activity.startActivity(intent);
            return true;
        }
        return false;
    }

    public static void initTextToSpeech(Activity activity) {
        Utils.textToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    Utils.textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }

    public static void stopTextToSpeech() {
        if (Utils.textToSpeech != null && Utils.textToSpeech.isSpeaking())
            Utils.textToSpeech.stop();
    }
}
