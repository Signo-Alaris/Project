package com.evanmorgan.stocktrader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends Activity {

    private ProgressDialog progress; //Displays "logging in..." screen when waiting for response
    String accountNo = null;
    String balance = null;
    boolean Buy = true;
    Handler PingHandler = new Handler();
    ArrayList<Instrument> instrumentList = new ArrayList<Instrument>();
    ArrayList<String> holdList = new ArrayList<String>();
    private ListView transactionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //Reload any previously saved instances
        setContentView(R.layout.activity_main); //Sets content view to the layout xml file
        transactionView = (ListView) findViewById(R.id.transactions);

        new Login(this).execute(); //When the app loads, create a login object
        new UpdateAcc(this).execute();
        new GetPrices(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new BuyMonitor(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new PingThread(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    private class UpdateAcc extends AsyncTask<Void, Object, String> {

        private Context context = null;

        private UpdateAcc(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(Void... params) {
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

                MainActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run() {
                            TextView balanceView = (TextView) findViewById(R.id.balanceField);
                            balanceView.setText("€" + balance);
                        }
                    }
                );
            }

            //Catch exception block
            catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class PingThread extends AsyncTask<Void, Object, String> {

        String responseTime = "";
        String url = "localhost";

        private Context context = null;

        private PingThread(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(Void... params) {
            while (true) {
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

                    Thread.sleep(3000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private class GetPrices extends AsyncTask<Void, Object, String> {
        private Context context = null;
        String instrumentURL = "SPX500_USD%2CUK100_GBP%2CCH20_CHF%2CNL25_EUR%2CJP225_USD%2CBCO_USD%2CSG30_SGD%2CDE30_EUR%2CUS30_USD%2CFR40_EUR%2CTWIX_USD%2CWTICO_USD";

        private GetPrices(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(Void... params) {
            while (true) {
                try {
                    URL GetPricesURL = new URL("https://api-fxpractice.oanda.com/v3/accounts/" + accountNo + "/pricing?instruments=" + instrumentURL); //Creates URL object with address to Oanda API
                    HttpURLConnection PriceConnection = (HttpURLConnection) GetPricesURL.openConnection(); //Open connection to website
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
                        if (count == j) {
                            tempAsk = scanner.next();
                        }

                        if (count == j + 1) {
                            tempBid = scanner.next();
                        }

                        if (count == j + 10) {
                            tempName = scanner.next();
                            tempName = tempName.split("\"")[0];
                            count = 1;

                            if (instrumentList.size() > 0) {
                                for (int i = 0; i < instrumentList.size(); i++) {
                                    if (tempName.equals((instrumentList.get(i)).instrumentName)) {
                                        if ((instrumentList.get(i)).history.size() >= 100) {
                                            (instrumentList.get(i)).history.remove(0); //Removes first price
                                        }
                                        (instrumentList.get(i)).history.add((float) (instrumentList.get(i)).askPrice); //Adds old ask price to the price history
                                        (instrumentList.get(i)).askPrice = Float.parseFloat(tempAsk); //Adds new ask price to instrument
                                        (instrumentList.get(i)).bidPrice = Float.parseFloat(tempBid); //Adds bew bid price to instrument
                                        searchCount = 0;
                                        break;
                                    } else if (searchCount == (instrumentList.size() - 1)) {
                                        instrumentList.add(new Instrument(tempName, Float.parseFloat(tempAsk), Float.parseFloat(tempBid))); //Add new instrument to instrument list
                                        System.out.println("New instrument added!: " + (instrumentList.get(i)).instrumentName + ", " + (instrumentList.get(i)).askPrice + ", " + (instrumentList.get(i)).bidPrice);
                                        searchCount = 0;
                                        break;
                                    } else {
                                        searchCount++;
                                    }
                                }
                            } else {
                                float tempAskFloat = Float.parseFloat(tempAsk);
                                float tempBidFloat = Float.parseFloat(tempBid);
                                Instrument newInstrument = new Instrument(tempName, tempAskFloat, tempBidFloat);
                                instrumentList.add(newInstrument); //Add new instrument to instrument list
                                System.out.println("First instrument added!: " + (instrumentList.get(0)).instrumentName + ", " + (instrumentList.get(0)).askPrice + ", " + (instrumentList.get(0)).bidPrice);
                                System.out.println(instrumentList.get(0).askPrice);
                            }
                        }
                        count++;

                        //Prevents possible NoSuchElement Exception
                        if (scanner.hasNext()) {
                            scanner.next();
                        }
                    }
                    scanner.close();

                    MainActivity.this.runOnUiThread(new Runnable() {
                                                          @Override
                                                          public void run() {
                                                              if(holdList.size()>0) {
                                                                  TextView textView = (TextView) findViewById(R.id.SharesMessage);
                                                                  textView.setText("");

                                                                  ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, holdList);
                                                                  transactionView.setAdapter(arrayAdapter);
                                                                  arrayAdapter.notifyDataSetChanged();
                                                              }
                                                          }
                                                      }
                    );
                    Thread.sleep(3000);
                }

                //Catch exception block
                catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class BuyMonitor extends AsyncTask<Void, Object, String> {
        private Context context = null;

        private BuyMonitor(Context c) {
            this.context = c;
        }

        @Override
        protected String doInBackground(Void... params) {
            while (true) {
                try {
                    for(int i=0; i<instrumentList.size(); i++)
                    {
                        //Compare the current price of each stock to the initial price and if the difference is 10%, buy
                        if((instrumentList.get(i)).askPrice < (((instrumentList.get(i)).history.get(0)*0.9)))
                        {
                            if(holdList.size()>0) {
                                for (int j = 0; j < holdList.size(); j++) {
                                    if (((instrumentList.get(i)).instrumentName).equals(holdList.get(j))) {
                                        Buy = false;
                                    }
                                }

                                if (Buy==true) {
                                    URL BuyMonitorURL = new URL("https://api-fxpractice.oanda.com/v3/accounts/" + accountNo + "/orders"); //Creates URL object with address to Oanda API
                                    HttpURLConnection PriceConnection = (HttpURLConnection) BuyMonitorURL.openConnection(); //Open connection to website
                                    String urlParameters = "{\"order\": {\"units\": \"100\", \"instrument\": \"" + ((instrumentList.get(i)).instrumentName) + "\",\"timeInForce\": \"FOK\",\"type\": \"MARKET\",\"positionFill\": \"DEFAULT\"}}";
                                    String requestType = "POST";
                                    PriceConnection.setRequestMethod(requestType); //Sets the sending method
                                    PriceConnection.setRequestProperty("USER-AGENT", "Mozilla/5.0"); //Sets the user-agent to mozilla
                                    PriceConnection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5"); //Sets the language
                                    PriceConnection.setRequestProperty("Authorization", "Bearer 5cac2883c147658ab1a41ddcf8357abb-9ad47a0f1ea244832de75d41f925db98");
                                    PriceConnection.setRequestProperty("Content-Type", "application/json");
                                    PriceConnection.setDoOutput(true); //Enables output sending, would not be needed with a GET, is needed for POST
                                    DataOutputStream dStream = new DataOutputStream(PriceConnection.getOutputStream()); //OutputStream object initialized with the connection's output stream gotten from getOutputStream
                                    dStream.writeBytes(urlParameters); //Send urlParameters as bytes of data
                                    dStream.flush(); //Empty the output cache
                                    dStream.close(); //Close the output object
                                    int responseCode = PriceConnection.getResponseCode(); //Receive the response code e.g. 200 or 404

                                    //Output data to terminal
                                    System.out.println("\nSending " + requestType + " request to URL : " + BuyMonitorURL);
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
                                    System.out.print(responseOutput);

                                    if (responseCode == 201) {
                                        holdList.add((instrumentList.get(i)).instrumentName);
                                        holdList.add(String.valueOf((instrumentList.get(i)).askPrice));
                                        holdList.add(String.valueOf(((instrumentList.get(i)).askPrice) * 100));
                                    }
                                }
                            }

                            else{
                                URL BuyMonitorURL = new URL("https://api-fxpractice.oanda.com/v3/accounts/" + accountNo + "/orders"); //Creates URL object with address to Oanda API
                                HttpURLConnection PriceConnection = (HttpURLConnection) BuyMonitorURL.openConnection(); //Open connection to website
                                String urlParameters = "{\"order\": {\"units\": \"100\", \"instrument\": \"" + ((instrumentList.get(0)).instrumentName) + "\",\"timeInForce\": \"FOK\",\"type\": \"MARKET\",\"positionFill\": \"DEFAULT\"}}";
                                String requestType = "POST";
                                PriceConnection.setRequestMethod(requestType); //Sets the sending method
                                PriceConnection.setRequestProperty("USER-AGENT", "Mozilla/5.0"); //Sets the user-agent to mozilla
                                PriceConnection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5"); //Sets the language
                                PriceConnection.setRequestProperty("Authorization", "Bearer 5cac2883c147658ab1a41ddcf8357abb-9ad47a0f1ea244832de75d41f925db98");
                                PriceConnection.setRequestProperty("Content-Type", "application/json");
                                PriceConnection.setDoOutput(true); //Enables output sending, would not be needed with a GET, is needed for POST
                                DataOutputStream dStream = new DataOutputStream(PriceConnection.getOutputStream()); //OutputStream object initialized with the connection's output stream gotten from getOutputStream
                                dStream.writeBytes(urlParameters); //Send urlParameters as bytes of data
                                dStream.flush(); //Empty the output cache
                                dStream.close(); //Close the output object
                                int responseCode = PriceConnection.getResponseCode(); //Receive the response code e.g. 200 or 404

                                //Output data to terminal
                                System.out.println("\nSending " + requestType + " request to URL : " + BuyMonitorURL);
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
                                System.out.print(responseOutput);

                                if (responseCode == 201) {
                                    holdList.add((instrumentList.get(i)).instrumentName);
                                    holdList.add(String.valueOf((instrumentList.get(i)).askPrice));
                                    holdList.add(String.valueOf(((instrumentList.get(i)).askPrice) * 100));
                                }
                            }
                        }
                    }
                    Thread.sleep(1500);
                }

                //Catch exception block
                catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}