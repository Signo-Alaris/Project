package com.evanmorgan.stocktrader;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Instrument {
    String instrumentName;
    float askPrice;
    float bidPrice;
    List<Float> history = new ArrayList<Float>();

    public Instrument() {
        instrumentName = "InstrumentNotAvailable";
        askPrice = 0;
        bidPrice = 0;
        history = new ArrayList<Float>();
    }

    public Instrument(String name, float ask, float bid) {
        instrumentName = name;
        askPrice = ask;
        bidPrice = bid;
        history.add(askPrice);
    }
};
