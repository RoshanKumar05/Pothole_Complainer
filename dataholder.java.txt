package com.example.potholecomplainer;

public class DbHolder {
    String latitude,longitude,image;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public DbHolder(String latitude, String longitude, String image) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.image = image;
    }
}
