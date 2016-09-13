package ivanrudyk.com.open_weather_api.helpers;

/**
 * Created by Yulian on 30.07.2016.
 */
import android.app.Activity;
import android.content.SharedPreferences;


import ivanrudyk.com.open_weather_api.ui.activity.*;


public class CityPreference {

    SharedPreferences prefs , prefs2;

    public CityPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
        prefs2 = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    public String getCity(){
        return prefs.getString("city", "Kiev, UA");
    }

    public void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }

    public String getNowURL(){
        return prefs.getString("url", MainActivity.BASE_CURRENT_WEATHER_URL_COORD);
    }

    public void setNowURL(String url){
        prefs.edit().putString("url", url).commit();
    }

}
