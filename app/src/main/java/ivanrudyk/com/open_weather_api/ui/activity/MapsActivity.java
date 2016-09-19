package ivanrudyk.com.open_weather_api.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import ivanrudyk.com.open_weather_api.R;
import ivanrudyk.com.open_weather_api.helpers.AlertDialogFragment;
import ivanrudyk.com.open_weather_api.helpers.FirebaseHelper;
import ivanrudyk.com.open_weather_api.helpers.Helper;
import ivanrudyk.com.open_weather_api.helpers.JSONWeatherParser;
import ivanrudyk.com.open_weather_api.helpers.RemoteFetch;
import ivanrudyk.com.open_weather_api.model.CurrentlyWeather;
import ivanrudyk.com.open_weather_api.model.Forecast;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private double[] coord = new double[]{0.0, 0.0};
    Handler handlerMaps;
    private Forecast mForecastMaps = new Forecast();
    private String BASE_CURRENT_WEATHER_URL_COORD = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&APPId=%s";
    private String BASE_DAILY_FORECAST_URL_COORD = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&lat=%s&lon=%s&units=metric&APPId=%s";
    private String BASE_HOURLY_FORECAST_URL_COORD = "http://api.openweathermap.org/data/2.5/forecast/hourly?mode=json&lat=%s&lon=%s&units=metric&APPId=%s";
    private final int DIALOG = 1;
    double latitude;
    double longitude;
    Drawable drawable;
    RelativeLayout mView;
    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView descriptonField;
    ImageView iconView;
    private GoogleApiClient client;
    Helper mHelper = new Helper();
    LatLng latLng;
    private String userName;
    private String uid;
    FirebaseHelper firebaseHelper = new FirebaseHelper();
    private CurrentlyWeather currentlyWeather;

    public MapsActivity() {
        handlerMaps = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        Intent intent = getIntent();
        uid = intent.getStringExtra("fnameMaps1");
        userName = intent.getStringExtra("fnameMaps2");

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mUiSettings = mMap.getUiSettings();

        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setAllGesturesEnabled(true);
        mUiSettings.setMapToolbarEnabled(true);
        mUiSettings.setIndoorLevelPickerEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.isMyLocationButtonEnabled();
        mUiSettings.isTiltGesturesEnabled();
        LocationManager locationManager = (LocationManager) getApplication()
                .getSystemService(LOCATION_SERVICE);
        int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if ((locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) && (result1 == PackageManager.PERMISSION_GRANTED)) {
            coord = mHelper.CoordTracker(getApplicationContext());
            latLng = new LatLng(coord[0], coord[1]);
            mMap.addMarker(new MarkerOptions().position(latLng).title("Your current position").
                    draggable(true)
                    .flat(true).icon(
                            BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .snippet(" Press any location on the map and get current weather there")
            );
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                showDialog(DIALOG);
            }
        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        mView = (RelativeLayout) getLayoutInflater()
                .inflate(R.layout.map_weather, null, false);
        mBuilder.setView(mView);
        mBuilder.setTitle("Current weather in this place : ")
                .setPositiveButton(MapsActivity.this.getString(R.string.error_ok_button_text), null)
                .setNegativeButton(R.string.add_to_favorite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        addLocationToFirebase();
                    }
                });
        return mBuilder.create();
    }

    private void addLocationToFirebase() {
        if (uid != null && userName != null){
            firebaseHelper.addDataLocation(userName, uid, currentlyWeather.mLocationCurrentWeather.getCity());
        }
        else {
            Toast.makeText(MapsActivity.this,
                    "Please, login before!!!",
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == DIALOG) {
            cityField = (TextView) dialog.getWindow().findViewById(R.id.city_field);
            updatedField = (TextView) dialog.getWindow().findViewById(R.id.updated_field);
            detailsField = (TextView) dialog.getWindow().findViewById(R.id.details_field);
            currentTemperatureField = (TextView) dialog.getWindow().findViewById(R.id.current_temperature_field);
            descriptonField = (TextView) dialog.getWindow().findViewById(R.id.decription_field);
            iconView = (ImageView) dialog.getWindow().findViewById(R.id.icon_Image);
            updateWeatherMapsData(latitude, longitude);
        }
    }

    protected void updateDisplayDialog() {
        currentlyWeather = mForecastMaps.getCurrent();
        cityField.setText(currentlyWeather.mLocationCurrentWeather.getCity().toUpperCase(Locale.US) +
                ", " +
                currentlyWeather.mLocationCurrentWeather.getCountry().toUpperCase(Locale.US));
        descriptonField.setText(currentlyWeather.mCurrentCondition.getCondition() + "(" + currentlyWeather.mCurrentCondition.getDescription() + ")");
        detailsField.setText("Humidity: " + String.format("%.0f", currentlyWeather.mCurrentCondition.getHumidity()) + "%" +
                "\n" + "Pressure: " + String.format("%.0f", currentlyWeather.mCurrentCondition.getPressure()) + " hPa" +
                "\n" + "Wind speed: " + String.format("%.0f", currentlyWeather.mWind.getSpeed()) + " mps" +
                "\n" + "Wind direction: " + String.format("%.0f", currentlyWeather.mWind.getDegree()) + "º" +
                "\n" + "Cloudness: " + currentlyWeather.mClouds.getPrecipitation() + " %"
        );
        currentTemperatureField.setText(String.format("%.0f", currentlyWeather.mTemperature.getTemperature()) + "℃");
        updatedField.setText("Last update: " + currentlyWeather.mLastUpdate.gettimeUpdate().toUpperCase(Locale.US));
        drawable = getResources().getDrawable(currentlyWeather.getIconId());
        iconView.setImageDrawable(drawable);
    }

    private void updateWeatherMapsData(final double tempLat, final double tempLon) {
        new Thread() {
            public void run() {
                if (Helper.isNetworkAvailable(getApplicationContext())) {
                    final String[] forecastUrl = new String[3];
                    String apiKey = "ddec71381c5621cdddefb5c58581e5bc";
                    try {
                        forecastUrl[0] = RemoteFetch.getCurrent(getApplicationContext(), (new URL(String.format(BASE_CURRENT_WEATHER_URL_COORD, tempLat, tempLon, apiKey))));
                        forecastUrl[1] = RemoteFetch.getDaily(getApplicationContext(), (new URL(String.format(BASE_DAILY_FORECAST_URL_COORD, tempLat, tempLon, apiKey))));
                        forecastUrl[2] = RemoteFetch.getHourly(getApplicationContext(), (new URL(String.format(BASE_HOURLY_FORECAST_URL_COORD, tempLat, tempLon, apiKey))));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    if ((forecastUrl[0] == null) && (Helper.isNetworkAvailable(getApplicationContext()))) {
                        handlerMaps.post(new Runnable() {
                            public void run() {
                                Toast.makeText(MapsActivity.this,
                                        getApplicationContext().getString(R.string.place_not_found),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if ((forecastUrl[0] != null) && (Helper.isNetworkAvailable(getApplicationContext()))) {
                        handlerMaps.post(new Runnable() {
                            public void run() {

                                try {
                                    mForecastMaps.setCurrent(JSONWeatherParser.getWeather(forecastUrl[0]));
                                    Log.e("CURRR", forecastUrl[0]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    mForecastMaps.setDailyForecast(JSONWeatherParser.getDailyForecast(forecastUrl[1]));
                                    Log.e("DAYYYYYY", forecastUrl[1]);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mForecastMaps.setHourlyForecast(JSONWeatherParser.getHourlyForecast(forecastUrl[2]));
                                    Log.e("HOURRRRR", forecastUrl[2]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateDisplayDialog();
                                    }
                                });
                            }
                        });
                    } else {
                        alertUserAboutError();
                    }
                } else if (!Helper.isNetworkAvailable(getApplicationContext())) {
                    handlerMaps.post(new Runnable() {
                        public void run() {

                            Toast.makeText(MapsActivity.this,
                                    getApplicationContext().getString(R.string.no_internet_connetion),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    alertUserAboutError();
                }
            }
        }.start();
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

}