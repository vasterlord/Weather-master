package ivanrudyk.com.open_weather_api.ui.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

import ivanrudyk.com.open_weather_api.R;
import ivanrudyk.com.open_weather_api.adapter.HourAdapter;
import ivanrudyk.com.open_weather_api.helpers.Helper;
import ivanrudyk.com.open_weather_api.model.CurrentlyWeather;
import ivanrudyk.com.open_weather_api.model.ForecastTransction;
import ivanrudyk.com.open_weather_api.model.HourlyWeather;


public class HourlyWeatherFragment extends Fragment {

    private HourlyWeather[] mHours;
    RecyclerView mRecyclerView;
    TextView mLocationLabel;
    public Helper mHelperH = new Helper();
    HourAdapter adapter;
    double [] coord = new double[2];
    RelativeLayout hourlyLayout;
    TextView mEmptyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View hourlyFragment = inflater.inflate(R.layout.fragment_hourly_weather, container, false);
        mRecyclerView = (RecyclerView) hourlyFragment.findViewById(R.id.reyclerView);
        mLocationLabel = (TextView) hourlyFragment.findViewById(R.id.locationLabel);
        hourlyLayout = (RelativeLayout) hourlyFragment.findViewById(R.id.hourly_layuot);
        mEmptyTextView = (TextView) hourlyFragment.findViewById(android.R.id.empty);
        mEmptyTextView.setVisibility(View.INVISIBLE);
        coord =  mHelperH.CoordTracker(getContext());
        if (ForecastTransction.getForecast() != null)
        {
            UpdateHourlyWeather();
        }
        else  if (ForecastTransction.getForecast()== null)
        {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        return hourlyFragment;
    }


    private void UpdateHourlyWeather() {
        mEmptyTextView.setVisibility(View.INVISIBLE);
        Parcelable[] parcelables = ForecastTransction.getForecast().getHourlyForecast();
        mHours = Arrays.copyOf(parcelables, parcelables.length, HourlyWeather[].class);
        adapter = new HourAdapter(getContext(), mHours);
        mRecyclerView.setAdapter(adapter);
        final CurrentlyWeather currentlyWeather = ForecastTransction.getForecast().getCurrent();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mLocationLabel.setText(currentlyWeather.mLocationCurrentWeather.getCity().toUpperCase(Locale.US) +
                ", " +
                currentlyWeather.mLocationCurrentWeather.getCountry().toUpperCase(Locale.US));
        mRecyclerView.setHasFixedSize(true);
    }


}
