package ivanrudyk.com.open_weather_api.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Locale;

import ivanrudyk.com.open_weather_api.R;
import ivanrudyk.com.open_weather_api.adapter.DayAdapter;
import ivanrudyk.com.open_weather_api.model.CurrentlyWeather;
import ivanrudyk.com.open_weather_api.model.DailyWeather;
import ivanrudyk.com.open_weather_api.model.ForecastTransction;


public class DailyWeatherFragment extends Fragment {

    TextView mLocationLabel;
    private DailyWeather[] mDays;
    Handler handler;
    ListView mListView;
    TextView mEmptyTextView;
    public DailyWeatherFragment() {handler = new Handler();}
    RelativeLayout dailyLayout;
    String location = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View dailyFragment = inflater.inflate(R.layout.fragment_daily_weather, container, false);
        mLocationLabel = (TextView) dailyFragment.findViewById(R.id.locationLabel);
        mListView = (ListView) dailyFragment.findViewById(android.R.id.list);
        mEmptyTextView = (TextView) dailyFragment.findViewById(android.R.id.empty);
        dailyLayout = (RelativeLayout) dailyFragment.findViewById(R.id.daily_layout);
        if (ForecastTransction.getForecast() != null)
        {
            UpdateDailyWeather();
        }
        else  if (ForecastTransction.getForecast()== null)
        {
            mListView.setEmptyView(mEmptyTextView);
        }
        return dailyFragment;
    }


    private void UpdateDailyWeather() {
        mListView.setEmptyView(mEmptyTextView);
        Parcelable[] parcelables = ForecastTransction.getForecast().getDailyForecast();
        mDays = Arrays.copyOf(parcelables, parcelables.length, DailyWeather[].class);
        mLocationLabel.setText(location);
        DayAdapter adapter = new DayAdapter(getContext(), mDays);
        final CurrentlyWeather currentlyWeather = ForecastTransction.getForecast().getCurrent();
        mListView.setAdapter(adapter);
        mLocationLabel.setText(currentlyWeather.mLocationCurrentWeather.getCity().toUpperCase(Locale.US) +
                ", " +
                currentlyWeather.mLocationCurrentWeather.getCountry().toUpperCase(Locale.US));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String dayOfTheWeek = mDays[position].getDayOfTheWeek();
                String conditions = mDays[position].getDescription();
                String highTemp = String.format("%.0f", mDays[position].avarageTemp());
                String message = String.format("У %s the avarage temperature will be %s℃ and it will be %s",
                        dayOfTheWeek,
                        highTemp,
                        conditions);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
