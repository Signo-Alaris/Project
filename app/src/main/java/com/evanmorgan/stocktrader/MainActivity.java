package com.evanmorgan.stocktrader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import static android.R.attr.button;


public class MainActivity extends Activity {

    private ProgressDialog progress; //Displays "logging in..." screen when waiting for response
    String accountNo = null;
    String balance = null;
    List<Instrument> instrumentList = new ArrayList<Instrument>();
    Handler PingHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Started");
        super.onCreate(savedInstanceState); //Reload any previously saved instances
        setContentView(R.layout.activity_main); //Sets content view to the layout xml file

        ImageButton buttonShares = (ImageButton) findViewById(R.id.imageButtonShares);
        buttonShares.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switchToShares(v);
            }
        });

        new Login(this).execute(); //When the app loads, create a login object
        new UpdateAcc(this).execute();
        startPropTimer();
        new GetPrices(this).execute();
    }

    @Override
    protected void onDestroy() {
        stopPropTimer();
        super.onDestroy();
    }

    protected void onResume(Bundle savedInstanceState) {
        super.onResume();
        System.out.println("Resumed");
        Handler PingHandler = new Handler();
        startPropTimer();
    }

    protected void onPause(Bundle savedInstanceState) {
        stopPropTimer();
        super.onPause();
    }

    private class Login extends AsyncTask<Object, Object, String> {

        private Context context = null;

        private Login(Context c) {
            this.context = c;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(this.context);
            progress.setMessage("Logging In...");
            progress.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                URL loginURL = new URL("https://api-fxpractice.oanda.com/v3/accounts"); //Creates URL object with address to Oanda API
                HttpURLConnection LoginConnection = (HttpURLConnection) loginURL.openConnection(); //Open connection to website
                String urlParameters = "";
                String requestType = "GET";
                LoginConnection.setRequestMethod(requestType); //Sets the sending method
                LoginConnection.setRequestProperty("USER-AGENT", "Mozilla/5.0"); //Sets the user-agent to mozilla
                LoginConnection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5"); //Sets the language
                LoginConnection.setRequestProperty("Authorization", "Bearer 5cac2883c147658ab1a41ddcf8357abb-9ad47a0f1ea244832de75d41f925db98");
                LoginConnection.setRequestProperty("Content-Type", "application/json");
                LoginConnection.setDoOutput(false);
                int responseCode = LoginConnection.getResponseCode(); //Receive the response code e.g. 200 or 404

                //Output data to terminal
                System.out.println("\nSending " + requestType + " request to URL : " + loginURL);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                //Create a buffer for the response from the website, read by getInputStream
                BufferedReader br = new BufferedReader(new InputStreamReader(LoginConnection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }

                br.close();

                //Read in Account Number from response output
                int count = 0;
                Scanner scanner = new Scanner(responseOutput.toString());
                scanner.useDelimiter("\"");
                while (scanner.hasNext()) {
                    if (count == 5) {
                        accountNo = (String) scanner.next();
                        System.out.println("Account: " + accountNo);
                        break;
                    }
                    count = count + 1;
                    scanner.next();
                }
                scanner.close();

                System.out.println("Background finished");
            }

            //Catch exception block
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return accountNo;
        }

        protected void onPostExecute(String result) {
            MainActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    TextView textView = (TextView) findViewById(R.id.accountField);
                                                    textView.setText(accountNo);

                                                    progress.dismiss();
                                                    System.out.println("Progress dismissed");
                                                }
                                            }
            );
        }
    }

    private class UpdateAcc extends AsyncTask<Object, Object, String> {

        private Context context = null;

        private UpdateAcc(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                URL AccDetailsURL = new URL("https://api-fxpractice.oanda.com/v3/accounts/" + accountNo); //Creates URL object with address to Oanda API
                HttpURLConnection AccConnection = (HttpURLConnection) AccDetailsURL.openConnection(); //Open connection to website
                String urlParameters = "";
                String requestType = "GET";
                AccConnection.setRequestMethod(requestType); //Sets the sending method
                AccConnection.setRequestProperty("USER-AGENT", "Mozilla/5.0"); //Sets the user-agent to mozilla
                AccConnection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5"); //Sets the language
                AccConnection.setRequestProperty("Authorization", "Bearer 5cac2883c147658ab1a41ddcf8357abb-9ad47a0f1ea244832de75d41f925db98");
                AccConnection.setRequestProperty("Content-Type", "application/json");
                AccConnection.setDoOutput(false); //Enables output sending, would not be needed with a GET, is needed for POST
                int responseCode = AccConnection.getResponseCode(); //Receive the response code e.g. 200 or 404

                //Output data to terminal
                System.out.println("\nSending " + requestType + " request to URL : " + AccDetailsURL);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                //Create a buffer for the response from the website, read by getInputStream
                BufferedReader br = new BufferedReader(new InputStreamReader(AccConnection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                //Read in balance from response output
                int count = 0;
                Scanner scanner = new Scanner(responseOutput.toString());
                scanner.useDelimiter("\"");

                while (scanner.hasNext()) {
                    if (count == 33) {
                        balance = (String) scanner.next();
                        System.out.println("Balance: \n" + Float.parseFloat(balance));
                        break;
                    }
                    count = count + 1;
                    scanner.next();
                }
                scanner.close();
            }

            //Catch exception block
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return balance;
        }

        protected void onPostExecute(String result) {
            MainActivity.this.runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run() {
                                                    TextView textView = (TextView) findViewById(R.id.balanceField);
                                                    textView.setText("€" + balance);
                                                }
                                            }
            );
        }
    }

    public class Instrument{
        public String instrumentName;
        public float askPrice;
        public float bidPrice;
        public List<Float> history = new ArrayList<Float>();

        private Instrument(String name, float ask, float bid){
            String instrumentName = name;
            float askPrice = ask;
            float bidPrice = bid;
            history.add(askPrice);
        }
    }

    private class GetPrices extends AsyncTask<Object, Object, String> {

        private Context context = null;
        String instrumentURL = "SPX500_USD%2CUK100_GBP%2CCH20_CHF%2CNL25_EUR%2CJP225_USD%2CBCO_USD%2CSG30_SGD%2CDE30_EUR%2CUS30_USD%2CFR40_EUR%2CTWIX_USD%2CWTICO_USD";

        private GetPrices(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                URL GetPricesURL = new URL("https://api-fxpractice.oanda.com/v3/accounts/" + accountNo + "/pricing?instruments=" + instrumentURL); //Creates URL object with address to Oanda API
                HttpURLConnection PriceConnection = (HttpURLConnection) GetPricesURL.openConnection(); //Open connection to website
                String urlParameters = "";
                String requestType = "GET";
                PriceConnection.setRequestMethod(requestType); //Sets the sending method
                PriceConnection.setRequestProperty("USER-AGENT", "Mozilla/5.0"); //Sets the user-agent to mozilla
                PriceConnection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5"); //Sets the language
                PriceConnection.setRequestProperty("Authorization", "Bearer 5cac2883c147658ab1a41ddcf8357abb-9ad47a0f1ea244832de75d41f925db98");
                PriceConnection.setRequestProperty("Content-Type", "application/json");
                PriceConnection.setDoOutput(false); //Enables output sending, would not be needed with a GET, is needed for POST
                int responseCode = PriceConnection.getResponseCode(); //Receive the response code e.g. 200 or 404

                //Output data to terminal
                System.out.println("\nSending " + requestType + " request to URL : " + GetPricesURL);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                //Create a buffer for the response from the website, read by getInputStream
                BufferedReader br = new BufferedReader(new InputStreamReader(PriceConnection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                //Read in balance from response output
                int count = 0, j = 3, searchCount = 0;
                String tempAsk = "", tempBid = "", tempName = "";
                Scanner scanner = new Scanner(responseOutput.toString());
                scanner.useDelimiter("price\":\"|\",\"liq\"|,\"instrument\":\"|\",\"");

                while (scanner.hasNext()) {
                    if(count == j)
                    {
                        tempAsk = scanner.next();
                    }

                    if(count == j+1)
                    {
                        tempBid = scanner.next();
                    }

                    if(count == j+10)
                    {
                        tempName = scanner.next();
                        tempName = tempName.split("\"")[0];
                        count = 1;

                        if(instrumentList.size()>0) {
                            for (int i = 0; i < instrumentList.size(); i++) {
                                if (tempName == ((instrumentList.get(i)).instrumentName)) {
                                    (instrumentList.get(i)).history.add((float) (instrumentList.get(i)).askPrice); //Adds old ask price to the price history
                                    (instrumentList.get(i)).askPrice = Float.parseFloat(tempAsk); //Adds new ask price to instrument
                                    (instrumentList.get(i)).bidPrice = Float.parseFloat(tempBid); //Adds bew bid price to instrument
                                    searchCount = 0;
                                    break;
                                }

                                else if (searchCount == (instrumentList.size()-1)) {
                                    instrumentList.add(new Instrument(tempName, Float.parseFloat(tempAsk), Float.parseFloat(tempBid))); //Add new instrument to instrument list
                                    System.out.println("New instrument added!: " + tempName + ", " + tempAsk + ", " + tempBid);
                                    searchCount=0;
                                    break;
                                }

                                else {
                                    searchCount++;
                                }
                            }
                        }

                        else
                        {
                            instrumentList.add(new Instrument(tempName, Float.parseFloat(tempAsk), Float.parseFloat(tempBid))); //Add new instrument to instrument list
                            System.out.println("New instrument added!: " + tempName + ", " + tempAsk + ", " + tempBid);
                        }
                    }
                    count++;

                    //Prevents possible NoSuchElement Exception
                    if(scanner.hasNext())
                    {
                        scanner.next();
                    }
                }
                scanner.close();
            }

            //Catch exception block
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return balance;
        }

        protected void onPostExecute(String result) {
            MainActivity.this.runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run() {

                                                }
                                            }
            );
        }
    }

    public Runnable pingThread = new Runnable() {
        String responseTime = "";

        @Override
        public void run() {
            String url = "localhost";
            try {
                //Create a buffer for the response from the website, read by getInputStream
                Process process = Runtime.getRuntime().exec("ping -c 1 " + url);
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                //Read in balance from response output
                int count = 0;
                Scanner scanner = new Scanner(responseOutput.toString());
                scanner.useDelimiter("time=|-");

                while (scanner.hasNext()) {
                    if (count == 1) {
                        responseTime = (String) scanner.next();
                        System.out.println("Ping1: " + responseTime);
                        break;
                    }
                    count = count + 1;
                    scanner.next();
                }
                scanner.close();

                MainActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        TextView textView = (TextView) findViewById(R.id.pingField);
                                                        textView.setText("Ping: " + responseTime);
                                                    }
                                                }
                );

                PingHandler.postDelayed(pingThread, 1500);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void startPropTimer() {
        pingThread.run();
    }

    private void stopPropTimer() {
        PingHandler.removeCallbacks(pingThread);
    }

    private void switchToShares(View view) {
        Intent intent = new Intent(this, SharesActivity.class);
        startActivity(intent);
    }
}