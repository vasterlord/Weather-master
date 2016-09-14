package ivanrudyk.com.open_weather_api.model;

/**
 * Created by Ivan on 14.09.2016.
 */
public  class WeatherUrl {
    public static String BASE_DAILY_FORECAST_URL_CITY = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&q=%s&units=metric&APPId=%s";
    public static String BASE_DAILY_FORECAST_URL_COORD = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&lat=%s&lon=%s&units=metric&APPId=%s";
    public static String BASE_HOURLY_FORECAST_URL_CITY = "http://api.openweathermap.org/data/2.5/forecast/hourly?mode=json&q=%s&units=metric&APPId=%s";
    public static String BASE_HOURLY_FORECAST_URL_COORD = "http://api.openweathermap.org/data/2.5/forecast/hourly?mode=json&lat=%s&lon=%s&units=metric&APPId=%s";
    public static final String BASE_CURRENT_WEATHER_URL_CITY = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&APPId=%s";
    public static final String BASE_CURRENT_WEATHER_URL_COORD = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&APPId=%s";

}
