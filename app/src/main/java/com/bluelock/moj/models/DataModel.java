package com.bluelock.moj.models;

import android.os.Parcel;
import android.os.Parcelable;


public class DataModel implements Parcelable {
    private final String filename;
    private final String filepath;

    protected DataModel(Parcel in) {
        filename = in.readString();
        filepath = in.readString();
    }

    public static final Creator<DataModel> CREATOR = new Creator<>() {
        @Override
        public DataModel createFromParcel(Parcel in) {
            return new DataModel(in);
        }

        @Override
        public DataModel[] newArray(int size) {
            return new DataModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(filename);
        parcel.writeString(filepath);
    }
}
