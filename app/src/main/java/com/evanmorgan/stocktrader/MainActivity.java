package com.evanmorgan.stocktrader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.evanmorgan.stocktrader.R;

import org.json.JSONObject;

import static android.R.attr.data;
import static android.R.attr.name;
import static android.R.attr.password;
import static java.lang.System.in;


public class MainActivity extends Activity {

    private ProgressDialog progress; //Displays "logging in..." screen when waiting for response

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //Reload any previously saved instances
        setContentView(R.layout.activity_main); //Sets content view to the layout xml file
    }

    public void sendPostRequest(View View) {
        new PostClass(this).execute();
    }

    private class PostClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public PostClass(Context c) {
            this.context = c;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Logging In...");
            progress.show();
        }

        //Opens the connection to the website, POSTs parameters and outputs response
        @Override
        protected Void doInBackground(String... params) {
            try {
                URL url = new URL("https://api-fxpractice.oanda.com/v3/accounts"); //Creates URL object with address to Oanda API

                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Open connection to website
                //Enter the parameters for the JSON object to be sent
                String urlParameters = "";
                String requestType = "GET";
                connection.setRequestMethod(requestType); //Sets the sending method
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0"); //Sets the user-agent to mozilla
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5"); //Sets the language
                connection.setRequestProperty("Authorization","Bearer 5cac2883c147658ab1a41ddcf8357abb-9ad47a0f1ea244832de75d41f925db98");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(false); //Enables output sending, would not be needed with a GET, is needed for POST
                /*DataOutputStream dStream = new DataOutputStream(connection.getOutputStream()); //OutputStream object initialized with the connection's output stream gotten from getOutputStream
                dStream.writeBytes(urlParameters); //Send urlParameters as bytes of data
                dStream.flush(); //Empty the output cache
                dStream.close(); //Close the output object */
                int responseCode = connection.getResponseCode(); //Receive the response code e.g. 200 or 404

                InputStream inputStream;

                int status = connection.getResponseCode();

                if (status != HttpURLConnection.HTTP_OK)
                    inputStream = connection.getErrorStream();
                else
                    inputStream = connection.getInputStream();

                //Output data to terminal
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                //Create a string builder object
                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + requestType);

                //Create a buffer for the response from the website, read by getInputStream
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                System.out.println("output===============" + br);
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                //Pass output to screen
                output.append(System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") + responseOutput.toString());

                //Read in Account Number from response output
                int count = 0;
                Scanner scanner = new Scanner(responseOutput.toString());
                scanner.useDelimiter("\"");
                while (scanner.hasNext())
                {
                    if(count==5) {
                        String accountNo = (String) scanner.next();
                        System.out.println("Account: " + accountNo);
                        break;
                    }
                    count = count + 1;
                    scanner.next();
                }
                scanner.close();

            //Catch block for catching exceptions
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute() {
            progress.dismiss();
        }

    }
}