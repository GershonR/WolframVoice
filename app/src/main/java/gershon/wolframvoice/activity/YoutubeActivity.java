package gershon.wolframvoice.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import gershon.wolframvoice.Auth;
import gershon.wolframvoice.BuildConfig;
import gershon.wolframvoice.MainActivity;
import gershon.wolframvoice.R;
import gershon.wolframvoice.Utils;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class YoutubeActivity extends AppCompatActivity implements
        YouTubePlayer.OnInitializedListener, TextToSpeech.OnInitListener {

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private YoutubeActivity youtubeActivity;
    private YouTubePlayerView youTubeView;
    private YouTube youtube;
    private YouTubePlayer youTubePlayer;
    private static boolean fullScreen;

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = Utils.textToSpeech.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this,
                        "This Language is not supported", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this,
                        "Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
            }
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this,
                    "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.youtube_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.youtube_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle b = getIntent().getExtras();
        String video = "kpnnBw5ANYI";
        if(b != null)
            video = b.getString("video");
        Utils.textToSpeech.speak("Playing " + video, TextToSpeech.QUEUE_FLUSH, null, null);

        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_fragment);

        YouTubePlayerSupportFragment frag =
                (YouTubePlayerSupportFragment) getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        frag.initialize(BuildConfig.YOUTUBE_DEVELOPER_KEY, this);

        new YoutubeSearchTask().execute(video);
        youtubeActivity = this;
    }


    @Override
    public void onBackPressed() {
        if (fullScreen){
            return;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(
                    getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer = player;
            youTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {

                @Override
                public void onFullscreen(boolean _isFullScreen) {
                    fullScreen = _isFullScreen;
                }
            });

            player.setPlayerStyle(PlayerStyle.DEFAULT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            getYouTubePlayerProvider().initialize(BuildConfig.YOUTUBE_DEVELOPER_KEY, this);
        }
    }

    private YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerView) findViewById(R.id.youtube_fragment);
    }

    private void setFullScreenSize() {
        ViewGroup.LayoutParams params = (ViewGroup.LayoutParams)youTubeView.getLayoutParams();

        params.height = youtubeActivity.getWindow().getDecorView().getHeight() - 120;

        youTubeView.setLayoutParams(params);
    }

    private class YoutubeSearchTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) throws IOException {
                    }
                }).setApplicationName("youtube-cmdline-search").build();



                YouTube.Search.List search = youtube.search().list("id,snippet");

                search.setKey(BuildConfig.YOUTUBE_DEVELOPER_KEY);
                search.setQ(strings[0]);

                search.setType("video");

                search.setFields("items(id/kind,id/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails/default/url),nextPageToken");
                search.setMaxResults((long)1);
                SearchListResponse searchResponse = search.execute();

                List<SearchResult> searchResultList = searchResponse.getItems();
                if (searchResultList != null) {
                    return searchResultList.get(0).getId().getVideoId();
                }
            } catch (GoogleJsonResponseException e) {
                System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                        + e.getDetails().getMessage());
            } catch (IOException e) {
                System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String temp) {
            if(youTubePlayer != null) {
                youTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {

                    @Override
                    public void onFullscreen(boolean _isFullScreen) {
                        fullScreen = _isFullScreen;
                    }
                });
                youTubePlayer.loadVideo(temp);
                setFullScreenSize();
            }
        }
    }

}
