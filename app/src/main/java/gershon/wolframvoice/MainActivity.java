package gershon.wolframvoice;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import gershon.wolframvoice.adapter.ResultPodAdapter;
import gershon.wolframvoice.model.ResultPod;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private Context context;
    private TextView mVoiceInputTv;
    private SpeechRecognizer sr;
    private ResultPodAdapter resultPodAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar progressBar;
    private String TAG = "MainActivity";

    private SearchTask searchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mVoiceInputTv = (TextView) findViewById(R.id.input);
        progressBar = (ProgressBar) findViewById(R.id.pbLoading);
        setSupportActionBar(toolbar);

        listener listener = new listener(this);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(listener);
        Utils.initTextToSpeech(this);

        findViewById(R.id.btnSpeak).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Utils.stopTextToSpeech();
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
                sr.startListening(intent);
            }
        });

        context = this;
        PermissionRequestUtil.requestAudioPermission(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // Setting the size as fixed improves the performance
        recyclerView.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        loadDefaultCard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadDefaultCard() {

        ResultPod resultPod = new ResultPod();
        resultPod.setTitle("Welcome to Wolfram Voice");
        resultPod.setDescription("Click on the microphone and ask anything");
        resultPod.setDefaultCard(true);

        List<ResultPod> resultPods = new ArrayList<>();
        resultPods.add(resultPod);

        resultPodAdapter = new ResultPodAdapter(resultPods);
        recyclerView.setAdapter(resultPodAdapter);
    }

    private void initiateSearch(String query, int searchType) {

        if (searchTask != null) {
            searchTask.cancel(true);
            searchTask = null;
        }

        String[] queryParameter = {query, searchType + ""};
        searchTask = new SearchTask();
        searchTask.execute(queryParameter);
    }

    class listener implements RecognitionListener {
        MainActivity mainActivity;

        listener(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }
        public void onReadyForSpeech(Bundle params) {
            //Log.d(TAG, "onReadyForSpeech");
        }

        public void onBeginningOfSpeech() {
           // Log.d(TAG, "onBeginningOfSpeech");
        }

        public void onRmsChanged(float rmsdB) {
           // Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            //Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
           // Log.d(TAG, "onEndofSpeech");
        }

        public void onError(int error) {
            //Log.d(TAG, "error " + error);
        }

        public void onResults(Bundle results) {
            //Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            mVoiceInputTv.setText(""+ data.get(0));
            progressBar.setVisibility(ProgressBar.VISIBLE);
            if(Utils.executeCommand(mainActivity,"" + data.get(0))) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                return;
            }
            Utils.textToSpeech.speak(Utils.getResponse(), TextToSpeech.QUEUE_FLUSH, null, null);
            initiateSearch("" + data.get(0), 1);
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

        private class SearchTask extends AsyncTask<String, Void, ArrayList<ResultPod>> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected ArrayList<ResultPod> doInBackground(String... params) {

                if (!Utils.isNetworkAvailable(context)) {
                    System.out.println("No Network");
                    return null;
                }


                return WolframAlphaAPI.getQueryResult(params[0]);

            }

            @Override
            protected void onPostExecute(ArrayList<ResultPod> resultPods) {
                super.onPostExecute(resultPods);

                if (resultPods != null && resultPods.size() > 0) {
                    populateResult(resultPods);
                }
            }
        }

        private void populateResult(ArrayList<ResultPod> resultPods) {
            resultPodAdapter = new ResultPodAdapter(resultPods);
            recyclerView.setAdapter(resultPodAdapter);

            String mainResult = null;

            try {
                mainResult = resultPods.get(1).getDescription();
            } catch (Exception e) {
                mainResult = resultPods.get(0).getDescription();
            }

            if (StringUtils.isNotEmpty(mainResult)) {
                Utils.stopTextToSpeech();
                Utils.textToSpeech.speak(mainResult, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
}
