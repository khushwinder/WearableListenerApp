package activitylifecycle.android.com.wearablelistenerapp;

import android.app.Notification;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by khushwinder on 13-Jun-16.
 */
public class ListenerServiceFromWear extends WearableListenerService {

    private static final String HELLO_WORLD_WEAR_PATH = "/hello-world-wear";
    //  private ProgressDialog progress;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        Log.i("ListenerServiceFromWear", "inside onMessageReceived");
        /*
         * Receive the message from wear
         */
        if (messageEvent.getPath().equals(HELLO_WORLD_WEAR_PATH)) {

            //do not show activity so commenting the below code. If u want u can uncomment it.

            /*Intent startIntent = new Intent(this, HandHeldActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);*/

            if (isNetworkAvailable()) {
                Log.i("ListenerServiceFromWear", "Sending REST call");
                new PostClass(this).execute(new String(messageEvent.getData()));
            } else {

                Log.i("ListenerServiceFromWear", "No internet available so sending through SMS");
                sendSMSMessage(new String(messageEvent.getData()));
                Log.i("ListenerServiceFromWear", "Sent SMS");
            }
        }

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendSMSMessage(String sapText) {
        sapText = "DDXRX " + sapText;
        Log.i("sendSMSMessage", sapText);
        sapText = sapText.replace("$","");
        Log.i("sendSMSMessage", sapText);
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("9220592205", null, sapText, null, null);
        } catch (Exception e) {
            Log.i("ListenerServiceFromWear", "Problem in sending SMS", e);
            e.printStackTrace();
        }
    }



    //Params,Progress,Result
    private class PostClass extends AsyncTask<String, Void, String> {

        private final Context context;

        public PostClass(Context c) {

            this.context = c;
//            this.error = status;
//            this.type = t;
        }


        @Override
        protected String doInBackground(String... params) {
            long restResponse = -1;
            String notificationText = "";
            try {

                String sapText = params[0];
              /*  if (sapText != null && !sapText.isEmpty() && !sapText.equals("null")){

                }*/
                Log.i("doInBackground sapText", sapText);
                int indDollar = sapText.indexOf("$");
                int indTo = sapText.indexOf("to");
                int indFrom = sapText.indexOf("from");
                //transfer $5 to Tim from City
                String pmtAmt = sapText.substring(indDollar+1, indTo - 1);
                String payeeName = sapText.substring(indTo + 3, indFrom - 1);
                String faName = sapText.substring(indFrom + 5, sapText.length());

                System.out.print("------------>" + pmtAmt + " " + payeeName + " " + faName);

                URL url = new URL("http://fisms.cf/pay");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                String urlParameters = "mobile=9876543210&" + "payee=" + payeeName + "&fundingAccount=" + faName + "&amount=" + pmtAmt;
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);
                //  connection.setConnectTimeout(10000);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                System.out.println("Sending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                // For logging purpose
               /* final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + "POST");*/
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

               // output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

                restResponse = Integer.parseInt(responseOutput.toString());
                if (restResponse >= 0) {
                    notificationText = notificationText.concat("Payment of $" + pmtAmt + " to "+ payeeName +" was successful.");
                } else {
                    notificationText = notificationText.concat("Payment of $" + pmtAmt + " to "+ payeeName +" was unsuccessful.");
                }

            } catch (MalformedURLException e) {
                Log.i("MalformedURLException", "Problem in calling services", e);
                return notificationText;
            } catch (IOException e) {
                Log.i("IOException", "Problem in calling services", e);
                return notificationText;
            } catch (Exception e) {
                Log.i("Exception", "Problem in calling services", e);
                return notificationText;
            }
            return notificationText;
        }

       /* protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }*/

        protected void onPostExecute(String notificationText) {

            Log.i("notification text ", notificationText);

            Notification notification = new NotificationCompat.Builder(getApplication())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("SAP Notification")
                    .setContentText(notificationText)
                    .setPriority(Notification.PRIORITY_MAX)
                    .extend(
                            new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                    .build();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());

            int notificationId = 1;
            notificationManager.notify(notificationId, notification);
        }
    }

}