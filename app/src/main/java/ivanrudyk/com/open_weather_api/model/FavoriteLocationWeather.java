package ivanrudyk.com.open_weather_api.model;

import java.util.ArrayList;

/**
 * Created by Ivan on 15.09.2016.
 */
public class FavoriteLocationWeather {

    private String city;
    private double temperature;
    private int imageSummary;
    public static ArrayList<String> listLocation = new ArrayList<>();

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getImageSummary() {
        return imageSummary;
    }

    public void setImageSummary(int imageSummary) {
        this.imageSummary = imageSummary;
    }
}
