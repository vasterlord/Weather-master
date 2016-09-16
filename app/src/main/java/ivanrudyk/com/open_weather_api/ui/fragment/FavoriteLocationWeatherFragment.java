package ivanrudyk.com.open_weather_api.ui.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ivanrudyk.com.open_weather_api.R;
import ivanrudyk.com.open_weather_api.adapter.FavoritesLocationAdapterWeather;
import ivanrudyk.com.open_weather_api.helpers.JSONWeatherParser;
import ivanrudyk.com.open_weather_api.helpers.RemoteFetch;
import ivanrudyk.com.open_weather_api.model.CurrentlyWeather;
import ivanrudyk.com.open_weather_api.model.FavoriteLocationWeather;
import ivanrudyk.com.open_weather_api.model.WeatherUrl;


public class FavoriteLocationWeatherFragment extends Fragment {


    RecyclerView.Adapter mRecyclerView;
    RecyclerView resV;
    TextView mLocationLabel;
    RelativeLayout favoriteLayout;
    TextView mEmptyTextView;
    FavoritesLocationAdapterWeather favoriteAdapter;
    private RecyclerView.LayoutManager llm;
    private String forecastUrl;
    CurrentlyWeather mCurrent = new CurrentlyWeather();
    private final String apiKey = "ddec71381c5621cdddefb5c58581e5bc";
    ArrayList<FavoriteLocationWeather> arrayListLocation = new ArrayList<>();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View favoriteLocationFragment = inflater.inflate(R.layout.fragment_favorite_location_weather, container, false);
        resV = (RecyclerView) favoriteLocationFragment.findViewById(R.id.reyclerViewFavorite);

        mLocationLabel = (TextView) favoriteLocationFragment.findViewById(R.id.locationLabel);
        favoriteLayout = (RelativeLayout) favoriteLocationFragment.findViewById(R.id.favorite_layuot);
        mEmptyTextView = (TextView) favoriteLocationFragment.findViewById(android.R.id.empty);
        mEmptyTextView.setVisibility(View.INVISIBLE);

        UpdateLocationWeather();


        return favoriteLocationFragment;
    }


    public void UpdateLocationWeather() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                do
                    try {
                        Thread.sleep(100);
                        Log.e("INTERNALLL: ", String.valueOf(FavoriteLocationWeather.listLocation.size()));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                while (FavoriteLocationWeather.listLocation.size() == 0);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

//                for (int i =0; i< FirebaseHelper.modelUser.getLocation().getLocation().size(); i++) {
//                    retriveWeatherData(FirebaseHelper.modelUser.getLocation().getLocation().get(i));
//                }

                return null;
            }


            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                retriveWeatherData(FavoriteLocationWeather.listLocation);


            }
        }.execute();


    }

    private void setAdapter(ArrayList<FavoriteLocationWeather> arrayListLocation) {
//        for (int i = 0; i<10 ; i++){
//            favoritLocWeather.setCity("dbn");
//            favoritLocWeather.setSummary("gnazb");
//            favoritLocWeather.setTemperature("34");
//            arrayListLocation.add(favoritLocWeather);
//        }
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        resV.setLayoutManager(layoutManager);
        favoriteAdapter = new FavoritesLocationAdapterWeather(getContext(), arrayListLocation);
        resV.setAdapter(favoriteAdapter);
        Log.e("INTERNALLL: ", "Set Adapter favorite");
        Log.e("INTERNALLL: ", String.valueOf(arrayListLocation.size()));
        resV.setHasFixedSize(true);
    }

    private void retriveWeatherData(final ArrayList<String> city) {
        arrayListLocation.clear();
        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                int j =0;
                if (FavoriteLocationWeather.listLocation.get(0).equals("")){
                    j++;
                }
                for (int i = j; i < FavoriteLocationWeather.listLocation.size(); i++) {
                    try {
                        forecastUrl = RemoteFetch.getCurrent(getContext(), (new URL(String.format(WeatherUrl.BASE_CURRENT_WEATHER_URL_CITY, city.get(i), apiKey))));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    if (forecastUrl == null) {

                    } else if (forecastUrl != null) {
                        try {
                            mCurrent = JSONWeatherParser.getWeather(forecastUrl);
                            Log.e("CURRR", forecastUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    FavoriteLocationWeather favoritLocWeather = new FavoriteLocationWeather();
                    favoritLocWeather.setCity(mCurrent.mLocationCurrentWeather.getCity());
                    favoritLocWeather.setSummary(mCurrent.mCurrentCondition.getDescription());
                    favoritLocWeather.setTemperature(mCurrent.mTemperature.getTemperature());
                    arrayListLocation.add(favoritLocWeather);
                    Log.e("LOG: ", "Arraylist loc size  = "+ arrayListLocation.size());
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setAdapter(arrayListLocation);
            }
        }.execute();

    }


}
