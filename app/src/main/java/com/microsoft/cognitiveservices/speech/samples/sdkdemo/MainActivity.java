package com.microsoft.cognitiveservices.speech.samples.sdkdemo;

import android.hardware.SensorEventListener;
import android.media.MediaPlayer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.hardware.SensorManager;


import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;
import com.spotify.android.appremote.api.Connector;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.LoginActivity;
import android.Manifest;

import android.database.Cursor;

import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.os.Handler;
import android.os.PowerManager;
import android.hardware.Sensor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;


import androidx.appcompat.app.AppCompatActivity;
import android.content.ActivityNotFoundException;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;
import java.util.List;
import androidx.annotation.NonNull;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;


import com.github.difflib.DiffUtils;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.util.Log;
import android.hardware.SensorEventListener;


import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentConfig;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGradingSystem;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentGranularity;
import com.microsoft.cognitiveservices.speech.PronunciationAssessmentResult;
import com.microsoft.cognitiveservices.speech.PropertyId;
import com.microsoft.cognitiveservices.speech.WordLevelTimingResult;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.intent.LanguageUnderstandingModel;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.intent.IntentRecognitionResult;
import com.microsoft.cognitiveservices.speech.intent.IntentRecognizer;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.CancellationDetails;
import com.microsoft.cognitiveservices.speech.KeywordRecognitionModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;


public class MainActivity extends AppCompatActivity {


    private PowerManager.WakeLock wakeLock;
    private Handler continuousRecognitionHandler;

    private MediaPlayer mediaPlayer;
    private static final int CONTINUOUS_RECOGNITION_INTERVAL = 10000;
    private SpeechRecognizer reco;
    private AudioConfig audioInput;

    private static final int PERMISSION_REQUEST_READ_CONTACTS = 1;


    private List<Contact> contactsList = new ArrayList<>();
    private TextToSpeech textToSpeech;

    private boolean awaitingConfirmation = false;
    private Contact pendingContact = null;

    // config for spotify
    private static final String CLIENT_ID = "67ef37a8174741d588bb2ce9e239f976"; // Replace with your actual client ID
    private static final String REDIRECT_URI = "https://com.spotify.android.luna/callback";
    private static final int SPOTIFY_REQUEST_CODE = 1337;
    private boolean isSpotifyLoggedIn = false;

    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private";


    private SpotifyAppRemote mSpotifyAppRemote;

    private Handler timeoutHandler = new Handler();

//    private MySpotifyAuthorizationActivity spotifyAuthorizationActivity;



    // Configuration for speech recognition
    //


    // Replace below with your own subscription key
    private static final String SpeechSubscriptionKey = "ee0bcd73c92f42689446d48b2959a978";
    // Replace below with your own service region (e.g., "westus").
    private static final String SpeechRegion = "eastus";

    private boolean isAwake = false;

    // Define the sleep timeout duration in milliseconds
    private static final long SLEEP_TIMEOUT = 6000; // 6 seconds


    private static final int PERMISSION_REQUEST_CODE = 123;

    //
    // Configuration for intent recognition
    //

    // Replace below with your own Language Understanding subscription key
    // The intent recognition service calls the required key 'endpoint key'.
    private static final String LanguageUnderstandingSubscriptionKey = "YourLanguageUnderstandingSubscriptionKey";
    // Replace below with the deployment region of your Language Understanding application
    private static final String LanguageUnderstandingServiceRegion = "YourLanguageUnderstandingServiceRegion";
    // Replace below with the application ID of your Language Understanding application
    private static final String LanguageUnderstandingAppId = "YourLanguageUnderstandingAppId";
    // Replace below with your own Keyword model file, kws.table model file is configured for "Computer" keyword
    private static final String KwsModelFile = "kws.table";
    private static final int YOUR_REQUEST_CODE = 1;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    private String lastAction;
    private static final String LOG_TAG = "YourActivity";

    private TextView recognizedTextView;

    private Button recognizeButton;
    private Button recognizeIntermediateButton;
    private Button recognizeContinuousButton;
    private Button recognizeIntentButton;
    private Button recognizeWithKeywordButton;
    private Button pronunciationAssessmentButton;
    private Button pronunciationAssessmentFromStreamButton;

    private MicrophoneStream microphoneStream;
    private MicrophoneStream createMicrophoneStream() {
        this.releaseMicrophoneStream();

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }
    private void releaseMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognizedTextView = findViewById(R.id.recognizedText);
        recognizedTextView.setMovementMethod(new ScrollingMovementMethod());

        recognizeButton = findViewById(R.id.buttonRecognize);
        recognizeContinuousButton = findViewById(R.id.buttonRecognizeContinuous);
        importContacts();

        // Register the accelerometer sensor
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorEventListener sensorEventListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                float zValue = event.values[2]; // Z-axis value
                if (zValue < -8.0) { // Phone is flipped downwards
                    Log.e("call manager", "call method reached");
                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (audioManager != null) {
                        // Mute all audio streams
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0); // Ringtone
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0); // Notifications
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0); // Alarms
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0); // Media playback
                        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0); // Voice calls
                        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0); // System sounds
                        Log.e("the volume is null", "the volume is null");
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not needed for this example
            }
        };
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);


        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);

            }
        });

        // Initialize SpeechSDK and request required permissions.
        try {
            // a unique number within the application to allow
            // correlating permission request responses with the request.
            int permissionRequestId = 5;

            // Request permissions needed for speech recognition
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, INTERNET, READ_EXTERNAL_STORAGE}, permissionRequestId);
        } catch (Exception ex) {
            Log.e("SpeechSDK", "could not init sdk, " + ex.toString());
            recognizedTextView.setText("Could not initialize: " + ex.toString());
        }

        // create config
        final SpeechConfig speechConfig;
        final KeywordRecognitionModel kwsModel;
        try {
            speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
            kwsModel = KeywordRecognitionModel.fromFile(copyAssetToCacheAndGetFilePath(KwsModelFile));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            displayException(ex);
            return;
        }

        try {
            audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
            reco = new SpeechRecognizer(speechConfig, audioInput);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            displayException(ex);
        }


        //////////////////////////////////////////////////////////////
        /////////////////SPOTIFY
        /////////////////////////////////////////////////////////////

        SpotifyAppRemote.connect(
                this,
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build(),
                new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d(LOG_TAG, "SpotifyAppRemote connected");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e(LOG_TAG, "SpotifyAppRemote connection failure: " + throwable.getMessage());
                    }
                }
        );


        ///////////////////////////////////////////////////
        // recognize
        ///////////////////////////////////////////////////
        recognizeContinuousButton.setOnClickListener(new View.OnClickListener() {
            private static final String logTag = "reco 3";
            private boolean continuousListeningStarted = false;
            private String buttonText = "";
            private ArrayList<String> content = new ArrayList<>();

            @Override
            public void onClick(final View view) {
                final Button clickedButton = (Button) view;
                disableButtons();
                if (!continuousListeningStarted) {
                    startSpeechRecognitionService();
                } else {
                    if (reco != null) {
                        final Future<Void> task = reco.stopContinuousRecognitionAsync();
                        setOnTaskCompletedListener(task, result -> {
                            Log.i(logTag, "Continuous recognition stopped.");
                            MainActivity.this.runOnUiThread(() -> {
                                clickedButton.setText(buttonText);
                                enableButtons();
                            });
                            enableButtons();
                            continuousListeningStarted = false;
                            if (continuousRecognitionHandler != null) {
                                continuousRecognitionHandler.removeCallbacksAndMessages(null);
                            }
                        });
                    } else {
                        continuousListeningStarted = false;
                    }
                    return;
                }

                clearTextBox();

                try {
                    content.clear();
                    audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
                    reco = new SpeechRecognizer(speechConfig, audioInput);

                    reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Intermediate result received: " + s);

                        // Check for wake-up keyword
                        if (s.toLowerCase().contains("hello")) {
                            isAwake = true;
                            speak("Yes, sir?");
                        } else if (isAwake) {  // Only process commands if awake
                            processCommand(s);
                        }

                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                        content.remove(content.size() - 1);
                    });

                    reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Final result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                    });

                    final Future<Void> task = reco.startContinuousRecognitionAsync();
                    setOnTaskCompletedListener(task, result -> {
                        continuousListeningStarted = true;
                        MainActivity.this.runOnUiThread(() -> {
                            buttonText = clickedButton.getText().toString();
                            clickedButton.setText("Stop");
                            clickedButton.setEnabled(true);
                        });
                    });

                    // Set a timer to go back to sleep if no speech is detected
                    continuousRecognitionHandler.postDelayed(() -> {
                        isAwake = false;
                        continuousListeningStarted = false;
                        if (continuousRecognitionHandler != null) {
                            continuousRecognitionHandler.removeCallbacksAndMessages(null);
                        }
                        Log.i(logTag, "Going back to sleep...");
                    }, 6000); // 6 seconds timeout
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    displayException(ex);
                }
            }
        });


        ///////////////////////////////////////////////////
        // recognize continuously
        ///////////////////////////////////////////////////


        recognizeContinuousButton.setOnClickListener(new View.OnClickListener() {
            private static final String logTag = "reco 3";
            private boolean continuousListeningStarted = false;
            private String buttonText = "";
            private ArrayList<String> content = new ArrayList<>();
            private Handler continuousRecognitionHandler;
            private boolean isAwake = false;

            @Override
            public void onClick(final View view) {
                final Button clickedButton = (Button) view;
                disableButtons();

                if (!continuousListeningStarted) {
                    startSpeechRecognitionService();
                } else {
                    if (reco != null) {
                        final Future<Void> task = reco.stopContinuousRecognitionAsync();
                        setOnTaskCompletedListener(task, result -> {
                            Log.i(logTag, "Continuous recognition stopped.");
                            MainActivity.this.runOnUiThread(() -> {
                                clickedButton.setText(buttonText);
                                enableButtons();
                            });
                            enableButtons();
                            continuousListeningStarted = false;
                            if (continuousRecognitionHandler != null) {
                                continuousRecognitionHandler.removeCallbacksAndMessages(null);
                            }
                        });
                    } else {
                        continuousListeningStarted = false;
                    }
                    return;
                }

                clearTextBox();

                try {
                    content.clear();
                    audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
                    reco = new SpeechRecognizer(speechConfig, audioInput);

                    reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Intermediate result received: " + s);

                        if (!isAwake && (s.toLowerCase().contains("hello") || s.toLowerCase().contains("hey") || s.toLowerCase().contains("artemis")))
                        {
                            playSound();
                            isAwake = true;
                            // Reset the sleep timer
                            continuousRecognitionHandler.removeCallbacksAndMessages(null);
                            continuousRecognitionHandler.postDelayed(() -> {
                                isAwake = false;
                                continuousListeningStarted = false;
                                if (continuousRecognitionHandler != null) {
                                    continuousRecognitionHandler.removeCallbacksAndMessages(null);
                                }
                                // Additional logic when going back to sleep
                                Log.i(logTag, "Going back to sleep...");
                            }, 10000); // 6 seconds timeout
                        }

                        if (isAwake) {
                            // Only process commands if awake
                            processCommand(s);
                        }

                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                        content.remove(content.size() - 1);
                    });

                    reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Final result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                    });

                    final Future<Void> task = reco.startContinuousRecognitionAsync();
                    setOnTaskCompletedListener(task, result -> {
                        continuousListeningStarted = true;
                        MainActivity.this.runOnUiThread(() -> {
                            buttonText = clickedButton.getText().toString();
                            clickedButton.setText("Stop");
                            clickedButton.setEnabled(true);
                        });
                    });

                    // Set a timer to go back to sleep if no speech is detected
                    continuousRecognitionHandler = new Handler();
                    continuousRecognitionHandler.postDelayed(() -> {
                        isAwake = false;
                        continuousListeningStarted = false;
                        if (continuousRecognitionHandler != null) {
                            continuousRecognitionHandler.removeCallbacksAndMessages(null);
                        }
                        Log.i(logTag, "Going back to sleep...");
                    }, 10000); // 6 seconds timeout

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    displayException(ex);
                }
            }
        });
    }


    public void startSpotifyAuthorization(Activity activity) {
        Log.d(LOG_TAG, "reached the startSpotifyAuthorization");
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{SCOPES});
        AuthorizationRequest request = builder.build();

        // Use the browser-based authorization
        AuthorizationClient.openLoginActivity(activity, SPOTIFY_REQUEST_CODE, request);
    }


    public void handleSpotifyAuthorizationResponse(int resultCode, Intent intent) {
        AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
        if (response.getType() == AuthorizationResponse.Type.TOKEN) {
            isSpotifyLoggedIn = true;
        }
        Log.d(LOG_TAG, "reached the handle auth response page");
        switch (response.getType()) {
            case TOKEN:
                // Handle successful response
                String accessToken = response.getAccessToken();
                // Use the access token to interact with the Spotify API
                Log.d("SpotifyAuth", "Access Token: " + accessToken);

                // Content linking logic
//                openSpotifyContentLink();

                // Update the recognized text in the main activity
                setRecognizedText("Spotify recognized. Welcome!");

                break;

            case ERROR:
                // Handle error response
                Log.e("SpotifyAuth", "Error: " + response.getError());

                // Update the recognized text in the main activity with the error message
                setRecognizedText("Spotify authorization failed. Error: " + response.getError());

                break;

            default:
                // Handle other cases
                break;
        }
    }

    private void openSpotifyContentLink(String songUri) {
        Log.d(LOG_TAG, "reached the open spotify link");
        boolean isSpotifyInstalled = isPackageInstalled("com.spotify.music");

        if (isSpotifyInstalled) {
            // Spotify is installed, open Spotify content link/ Replace with the URI of the desired song
            final String branchLink = "https://spotify.link/content_linking?~campaign=" + getPackageName() +
                    "&$deeplink_path=" + songUri + "&$fallback_url=" + songUri;

            Log.d("SpotifyAuth", "Opening Spotify content link: " + branchLink);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(branchLink));
            startActivity(intent);
        } else {
            Log.e(LOG_TAG, "Spotify not installed.");
        }
    }

    private void processCommand(String command) {
        // Implement your command processing logic here
        // Example: Check for specific commands and take appropriate actions
        if (command.toLowerCase().contains("sleep")) {
            moveAppToBackground();
        } else if (command.toLowerCase().contains("camera")) {
            bringCameraToForeground();
            speak("Camera opened.");
        } else if (command.toLowerCase().contains("gallery")) {
            bringGalleryToForeground();
            speak("Gallery opened.");
        } else if (command.toLowerCase().contains("play")) {
            controlPlayback(true);
            speak("Playing.");
        } else if (command.toLowerCase().contains("pause")) {
            controlPlayback(false);
            speak("Pausing.");
        } else if (command.toLowerCase().contains("authorize spotify")) {
            startSpotifyAuthorization(MainActivity.this);
            speak("Spotify authorization successful");
        } else if (command.toLowerCase().contains("search")) {
            String searchQuery = extractSearchQuery(command);
            if (!searchQuery.isEmpty()) {
                searchSong(searchQuery);
                speak("Playing " + searchQuery + " on Spotify");
            } else {
                Log.e(LOG_TAG, "Empty search query");
            }
        } else if (command.toLowerCase().contains("stop")) {
            stopSpeechRecognitionService();
            speak("The app is exiting.");
        }else if (command.toLowerCase().contains("call")) {
            Log.e(LOG_TAG, "entered the call method");
            // Extract the name from the command
            String spokenName = extractName(command);
            // Find the nearest match for the spoken name
            Contact matchedContact = findNearestMatch(spokenName);
            Log.e(LOG_TAG, String.valueOf(matchedContact));
            if (matchedContact != null) {
                pendingContact = matchedContact;
                if (pendingContact != null) {
                    Log.e(LOG_TAG, "entered the pendingcontact");
                    String phoneNumber = matchedContact.getPhoneNumber();
                    if (!TextUtils.isEmpty(phoneNumber)) {
                        Log.e(LOG_TAG, "entered the is empty");
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + phoneNumber));
                        startActivity(callIntent);
                    } else {
                        // Handle scenario when no phone number is available
                        speak("Sorry, no phone number found for this contact.");
                    }
                } else {
                    // Handle scenario when pending contact is null
                    speak("Sorry, something went wrong. Please try again.");
                }
            }
        }

            else if (command.toLowerCase().contains("stop")) {
        stopSpeechRecognitionService();
        speak("The app is exiting.");
        }
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    ////////////////////////////////////
    // calling functionality
    ///////////////////////////////////

    private void confirmCall(Contact contact, String recognizedName) {
        // Ask for confirmation before calling the contact
        String confirmationMessage = "Do you want to call " + contact.getName() + "?";
        speak(confirmationMessage);
        Log.e(LOG_TAG, "Recognized Contact Name: " + contact.getName()); // Print the recognized contact name
        // Set a flag to indicate that confirmation is awaited
        awaitingConfirmation = true;
        // Save the contact for pending call
        pendingContact = contact;
    }


    private void importContacts() {
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                String phoneNumber = cursor.getString(phoneNumberIndex);
                contactsList.add(new Contact(name, phoneNumber));
            }

            cursor.close();
        }
    }

    private Contact findNearestMatch(String spokenName) {
        Contact nearestMatch = null;
        int minDistance = Integer.MAX_VALUE;

        for (Contact contact : contactsList) {
            int distance = calculateLevenshteinDistance(contact.getName(), spokenName);
            if (distance < minDistance) {
                minDistance = distance;
                nearestMatch = contact;
            }
        }

        return nearestMatch;
    }

    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private String extractName(String recognizedContent) {
        // Define a regular expression pattern to match names
        Pattern pattern = Pattern.compile("call\\s+(\\w+)");
        Matcher matcher = pattern.matcher(recognizedContent.toLowerCase());

        if (matcher.find()) {
            // Extract the name from the matched group
            return matcher.group(1);
        } else {
            return ""; // Return an empty string if no name is found
        }
    }






    private void controlPlayback(boolean play) {
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            if (play) {
                // Start playback
                mSpotifyAppRemote.getPlayerApi().resume();
            } else {
                // Pause playback
                mSpotifyAppRemote.getPlayerApi().pause();
            }
        } else {
            Log.e(LOG_TAG, "SpotifyAppRemote is not connected.");
        }
    }

    public void searchSong(String query) {
        try {
            // Use your Spotify Web API search endpoint URL with the appropriate query parameters
            String apiUrl = "https://spotify23.p.rapidapi.com/search/?q=" + URLEncoder.encode(query, "UTF-8")
                    + "&type=track&offset=0&limit=1&numberOfTopResults=1";

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .get()
                    .addHeader("X-RapidAPI-Key", "65ddafd97fmsh1c40b1aad0c0e55p188efdjsn90dd67f4d815")
                    .addHeader("X-RapidAPI-Host", "spotify23.p.rapidapi.com")
                    .build();

            Response response = client.newCall(request).execute();
            Log.e(LOG_TAG, "reached the search song func");
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                // Parse the response and extract the URI of the first track
                String songUri = extractSongUri(responseBody);

                // Perform actions based on the search result
                if (songUri != null && !songUri.isEmpty()) {
                    // Do something with the song URI, such as playing it
                    openSpotifyContentLink(songUri);
                } else {
                    Log.e(LOG_TAG, "No matching song found");
                }
            } else {
                Log.e(LOG_TAG, "Search request failed with code: " + response.code());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Error during search: " + ex.getMessage());
        }
    }

    public String extractSongUri(String responseBody) {
        try {
            JSONObject responseJson = new JSONObject(responseBody);

            // Assuming 'topResults' is an object in the response
            JSONObject topResults = responseJson.getJSONObject("topResults");

            // Assuming 'items' is an array under 'topResults'
            JSONArray itemsArray = topResults.getJSONArray("items");

            // Check if there is at least one item in the array
            if (itemsArray.length() > 0) {
                JSONObject firstItem = itemsArray.getJSONObject(0);

                // Assuming 'data' is an object under 'items'
                JSONObject dataObject = firstItem.getJSONObject("data");

                // Assuming 'uri' is a key under 'data'
                String songUri = dataObject.getString("uri");

                return songUri;
            } else {
                Log.e(LOG_TAG, "No items found in the response");
                return null;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing JSON response: " + e.getMessage());
            return null;
        }
    }

    private String extractSearchQuery(String userCommand) {
        // Assuming that the search query comes after the keyword "search"
        // You may need to adjust this logic based on your specific use case
        String[] commandParts = userCommand.split("search", 2);

        // Check if there is a second part after "search"
        if (commandParts.length > 1) {
            // Trim the extracted search query
            return commandParts[1].trim();
        } else {
            return "";
        }
    }

    private void displayException(Exception ex) {
        recognizedTextView.setText(ex.getMessage() + System.lineSeparator() + TextUtils.join(System.lineSeparator(), ex.getStackTrace()));
    }

    private void moveAppToBackground() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            moveTaskToBack(true); // For older versions, move the task to the back
        } else {
            // For newer versions, minimize the app without finishing the task
            moveTaskToBack(false);
        }
    }


    /////////////////////////////////////////////////////////
    ////////this is the part of the foreground service
    /////////////////////////////////////////////////////////

    private void startSpeechRecognitionService() {
        Intent serviceIntent = new Intent(this, SpeechRecognitionService.class);
        startService(serviceIntent);
    }

    private void stopSpeechRecognitionService() {
        Intent serviceIntent = new Intent(this, SpeechRecognitionService.class);
        stopService(serviceIntent);
    }

//    @Override
//    protected void onDestroy() {
//        // Stop the SpeechRecognitionService when the activity is destroyed
//        stopSpeechRecognitionService();
//
//        super.onDestroy();
//    }




    private void playSound() {
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.test);
        mediaPlayer.setOnCompletionListener(mp -> {
            // Release MediaPlayer resources after sound completion
            mediaPlayer.release();
            mediaPlayer = null;
        });
        mediaPlayer.start();
    }

    // this is the code to bring gallery to foreground

    private void bringGalleryToForeground() {
        // Check if the app is in the foreground, and bring it to the foreground if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // The permission is not granted, request it
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted, proceed with bringing the gallery app to the foreground
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Optional, already in foreground

            try {
                startActivity(intent);
                Log.d("GalleryOpen", "Gallery opened successfully.");
            } catch (ActivityNotFoundException e) {
                Log.e("GalleryOpen", "Error opening gallery: " + e.getMessage());
                // Handle the case where no activity is available to handle the Intent
            }
        }
    }

    // Function to speak a given text
    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void bringCameraToForeground() {
        // Check if the app is in the foreground, and bring it to the foreground if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // The permission is not granted, request it
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted, proceed with bringing the camera app to the foreground
            Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Optional, already in foreground

            try {
                startActivity(intent);
                Log.d("CameraOpen", "Camera opened successfully.");
            } catch (ActivityNotFoundException e) {
                Log.e("CameraOpen", "Error opening camera: " + e.getMessage());
                // Handle the case where no activity is available to handle the Intent
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if the user granted the SYSTEM_ALERT_WINDOW permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // Permission granted, proceed with the last action
                if (lastAction.equals("gallery")) {
                    bringGalleryToForeground();
                } else if (lastAction.equals("camera")) {
                    bringCameraToForeground();
                }
                else if (lastAction.equals("importContacts")) {
                    // Import contacts since permission is granted
                    importContacts();
                }
            }
        } else if (requestCode == SPOTIFY_REQUEST_CODE) {
            // Handle Spotify authorization response
            handleSpotifyAuthorizationResponse(resultCode, data);  // Updated this line
        } else {
            // Permission not granted, handle accordingly (e.g., show a message to the user)
            Log.e("Foreground", "SYSTEM_ALERT_WINDOW permission not granted.");
        }

    }




    private void clearTextBox() {
        AppendTextLine("", true);
    }



    private void setRecognizedText(final String s) {
        AppendTextLine(s, true);
    }

    private void AppendTextLine(final String s, final Boolean erase) {
        MainActivity.this.runOnUiThread(() -> {
            if (erase) {
                recognizedTextView.setText(s);
            } else {
                String txt = recognizedTextView.getText().toString();
                recognizedTextView.setText(txt + System.lineSeparator() + s);
            }
        });
    }

    private void disableButtons() {
        MainActivity.this.runOnUiThread(() -> {
            recognizeButton.setEnabled(false);
            recognizeContinuousButton.setEnabled(false);
        });
    }

    private void enableButtons() {
        MainActivity.this.runOnUiThread(() -> {
            recognizeButton.setEnabled(true);
            recognizeContinuousButton.setEnabled(true);

        });
    }

    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    private String copyAssetToCacheAndGetFilePath(String filename) {
        File cacheFile = new File(getCacheDir() + "/" + filename);
        if (!cacheFile.exists()) {
            try {
                InputStream is = getAssets().open(filename);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                FileOutputStream fos = new FileOutputStream(cacheFile);
                fos.write(buffer);
                fos.close();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return cacheFile.getPath();
    }

    private static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }

    public class Word {
        public String word;
        public String errorType;
        public double accuracyScore;
        public long duration;
        public long offset;
        public Word(String word, String errorType) {
            this.word = word;
            this.errorType = errorType;
        }

        public Word(String word, String errorType, double accuracyScore, long duration, long offset) {
            this(word, errorType);
            this.accuracyScore = accuracyScore;
            this.duration = duration;
            this.offset = offset;
        }
    }
}
class Contact {
    private String name;
    private String phoneNumber;

    public Contact(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
