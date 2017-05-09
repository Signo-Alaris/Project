package com.evanmorgan.stocktrader;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class SharesActivity extends AppCompatActivity {

    Handler PingHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shares);

        ImageButton buttonShares = (ImageButton) findViewById(R.id.imageButtonHome);
        buttonShares.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switchToHome(v);
            }
        });

        startPropTimer();
    }

    private Runnable pingThread = new Runnable() {
        String responseTime = "";

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
                        System.out.println("Ping2: " + responseTime);
                        break;
                    }
                    count = count + 1;
                    scanner.next();
                }
                scanner.close();

                SharesActivity.this.runOnUiThread(new Runnable() {
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

    private void switchToHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
