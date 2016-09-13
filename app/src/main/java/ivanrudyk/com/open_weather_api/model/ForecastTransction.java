package ivanrudyk.com.open_weather_api.model;

/**
 * Created by Ivan on 19.08.2016.
 */
public  class ForecastTransction {
    private static Forecast forecast;

    public static Forecast getForecast() {
        return forecast;
    }

    public static void setForecast(Forecast forecast) {
        ForecastTransction.forecast = forecast;
    }

}
