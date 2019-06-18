package it.crisoftdev.sportz.obj;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class MyLocation implements Serializable, Parcelable {
    private double latitude;
    private double longitude;

    public MyLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected MyLocation(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<MyLocation> CREATOR = new Creator<MyLocation>() {
        @Override
        public MyLocation createFromParcel(Parcel in) {
            return new MyLocation(in);
        }

        @Override
        public MyLocation[] newArray(int size) {
            return new MyLocation[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
