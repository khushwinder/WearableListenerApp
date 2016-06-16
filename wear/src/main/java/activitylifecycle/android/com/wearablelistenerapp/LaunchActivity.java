package activitylifecycle.android.com.wearablelistenerapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Locale;

public class LaunchActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    Node mNode; // the connected device to send the message to
    GoogleApiClient mGoogleApiClient;
    private static final String HELLO_WORLD_WEAR_PATH = "/hello-world-wear";
    private boolean mResolvingError = false;
    EditText sapEditText;
    TextView helpText;
    private static final int SPEECH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wearable);


        //Connect the GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //UI elements with a simple CircleImageView
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                // CircledImageView mCircledImageView = (CircledImageView) stub.findViewById(R.id.circle);
                ImageButton mCircledImageView = (ImageButton) stub.findViewById(R.id.mic);
                sapEditText = (EditText) findViewById(R.id.sapText);
                helpText = (TextView) findViewById(R.id.helpText);
                sapEditText.setText("");
                //Listener to send the message (it is just an example)
                mCircledImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //  if (isNetworkAvailable()) {
                        //   Log.i("LaunchActivity ", "Network  available");

                        // calling voice recognitions
                        displaySpeechRecognizer();
                        Log.i("displaySpeechRecogn ", "after displaySpeechRecognizer");
                        // validation text
                           /* if (sapEditText.getError() != null) {
                                Log.i("else ", "inside if");
                                return;
                            } else {
                                Log.i("else ", "inside else");
                                sendMessage(String.valueOf(sapEditText.getText()));
                            }*/
                       /* } else {
                            Log.i("LaunchActivity ", "Network not available");
                            if (!validateSapText(String.valueOf(sapEditText.getText()))) {
                                sapEditText.setError("Invalid txt");
                                // sapText.setError(Html.fromHtml("<font color='red'>Invalid Format</font>"));
                                //helpText.setVisibility(View.VISIBLE);
                                //  return;
                            }
                        }*/
                    }

                });
            }
        });
    }

   /* private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/

    public boolean validateSapText(String sapText) {
        //Transfer 333 to Payee from ICICI
        boolean validate = false;
        if (0 == sapText.length()) {
            return validate;
        }
        int indPay;
        if (sapText.indexOf("Transfer ") != -1) {
            indPay = sapText.indexOf("Transfer ");
        } else {
            indPay = sapText.indexOf("transfer ");
        }
        int intDollar = 0;
        if (sapText.indexOf("$ ") != -1) {
            intDollar = sapText.indexOf("$ ");
        }
        int indTo = sapText.indexOf(" to ");
        int indFrom = sapText.indexOf(" from ");

        if (indPay != -1 && indTo != -1 && indFrom != -1 && intDollar != -1) {
            validate = true;
        }
        return validate;
    }

    /**
     * Send message to mobile handheld
     */
    private void sendMessage(String text) {
        Log.i("LaunchActivity ", "inside sendMessage the text sending is " + text);
        if (mNode != null && mGoogleApiClient != null && mGoogleApiClient.isConnected() && text != null) {
            Log.i("LaunchActivity ", "sending message");
            final byte[] volume = text.getBytes();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode.getId(), HELLO_WORLD_WEAR_PATH, volume).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e("TAG", "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
            Log.i("LaunchActivity ", "message sent");
        } else {
            //Improve your code
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    /*
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    mNode = node;
                }
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Improve your code
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Improve your code
    }


    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.forLanguageTag("en-IN"));
        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, helpText.getText().toString());

// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        Log.i("LaunchActivity ", "requestCode " + String.valueOf(requestCode));
        Log.i("LaunchActivity ", "resultCode " + String.valueOf(resultCode));
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.i("spoken test is ", spokenText);
            // Do something with spokenText
            sapEditText.setText(spokenText);
            if (!validateSapText(String.valueOf(sapEditText.getText()))) {
                Log.i(" LaunchActivity", "spoken test is invalid");
                sapEditText.setError("Invalid txt");
            } else {
                Log.i("LaunchActivity ", "spoken test is valid");
                sapEditText.setError(null);
                sendMessage(spokenText);
            }
        } else {
            Log.i(" LaunchActivity", "Something wrong with mic");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}