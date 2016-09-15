package ivanrudyk.com.open_weather_api.ui.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import ivanrudyk.com.open_weather_api.R;
import ivanrudyk.com.open_weather_api.helpers.AlertDialogFragment;
import ivanrudyk.com.open_weather_api.helpers.CityPreference;
import ivanrudyk.com.open_weather_api.helpers.ComplexPreferences;
import ivanrudyk.com.open_weather_api.helpers.FirebaseHelper;
import ivanrudyk.com.open_weather_api.helpers.Helper;
import ivanrudyk.com.open_weather_api.helpers.JSONWeatherParser;
import ivanrudyk.com.open_weather_api.helpers.RealmDbHelper;
import ivanrudyk.com.open_weather_api.helpers.RemoteFetch;
import ivanrudyk.com.open_weather_api.model.CurrentlyWeather;
import ivanrudyk.com.open_weather_api.model.Forecast;
import ivanrudyk.com.open_weather_api.model.ForecastTransction;
import ivanrudyk.com.open_weather_api.model.ModelLocation;
import ivanrudyk.com.open_weather_api.model.ModelUser;
import ivanrudyk.com.open_weather_api.presenter.activity.MainPresenter;
import ivanrudyk.com.open_weather_api.presenter.activity.MainPresenterImplement;
import ivanrudyk.com.open_weather_api.ui.fragment.DailyWeatherFragment;
import ivanrudyk.com.open_weather_api.ui.fragment.FavoriteLocationWeatherFragment;
import ivanrudyk.com.open_weather_api.ui.fragment.HourlyWeatherFragment;
import ivanrudyk.com.open_weather_api.ui.fragment.NavigationDraverFragment;

public class MainActivity extends AppCompatActivity implements MainView, NavigationDraverFragment.onSomeEventListenerDraver, View.OnClickListener {


    private Toolbar toolbar;
    private ImageView imOk;
    private TextView etRegister;
    private EditText etLogin, etPassword;
    private ProgressBar progressBar;
    private LoginButton loginButtonFacebook;
    private ImageButton ibLogin;
    private CheckBox checkBoxShowPassword;
    private final int REGISTER_REQUEST = 34323;
    private String uid = "";
    private double[] coord = new double[2];
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private MainPresenter presenter;
    private ModelUser users = new ModelUser();
    private ModelLocation modelLocation = new ModelLocation();
    private RealmDbHelper dbHelper = new RealmDbHelper();
    private Profile profile;
    private int inputType;
    private Dialog d;
    private FirebaseHelper firebaseHelper = new FirebaseHelper();
    private CallbackManager mCallbackManager;
    //-------------------------------------------------------------------------------------------------------------------
    private static String BASE_DAILY_FORECAST_URL_CITY = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&q=%s&units=metric&APPId=%s";
    private static String BASE_DAILY_FORECAST_URL_COORD = "http://api.openweathermap.org/data/2.5/forecast/daily?mode=json&lat=%s&lon=%s&units=metric&APPId=%s";
    private static String BASE_HOURLY_FORECAST_URL_CITY = "http://api.openweathermap.org/data/2.5/forecast/hourly?mode=json&q=%s&units=metric&APPId=%s";
    private static String BASE_HOURLY_FORECAST_URL_COORD = "http://api.openweathermap.org/data/2.5/forecast/hourly?mode=json&lat=%s&lon=%s&units=metric&APPId=%s";


    //-------------------------------------------------------------------------------------------------------------------
    public MainActivity() {
        handler = new Handler();
    }

    public static final String BASE_CURRENT_WEATHER_URL_CITY = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&APPId=%s";
    public static final String BASE_CURRENT_WEATHER_URL_COORD = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&APPId=%s";
    public String nowURL = BASE_CURRENT_WEATHER_URL_COORD;
    public static final String TAG = MainActivity.class.getSimpleName();
    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
//                AccessToken accessToken = loginResult.getAccessToken();
//                handleFacebookAccessToken(loginResult.getAccessToken());
//                Log.e(TAG, "SsssssignInnn");
            mAuth.signOut();
            dbHelper.deleteUserFromRealm(getApplicationContext());
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException error) {

        }
    };

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private TextView cityField;
    private TextView updatedField;
    private TextView detailsField;
    private TextView currentTemperatureField;
    private TextView descriptonField;
    private ImageView iconView;
    private ImageView mRefreshImageView;
    private ProgressBar mProgressBar;
    private TextView mEmptyTextView;
    private Handler handler;
    public Forecast mForecast = new Forecast();
    public Helper mHelper = new Helper();
    private Drawable drawable;
    private String city = "";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private final HourlyWeatherFragment hourlyWeatherFragment = new HourlyWeatherFragment();
    private final DailyWeatherFragment dailyWeatherFragment = new DailyWeatherFragment();
    private final FavoriteLocationWeatherFragment favoriteLocationWeatherFragment = new FavoriteLocationWeatherFragment();
    HeadActivityTask mt;

    @Override
    public void eventMapsOpen(String s) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void eventChangeSity() {
        if (!Helper.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(MainActivity.this,
                    getApplicationContext().getString(R.string.no_internet_connetion),
                    Toast.LENGTH_LONG).show();
        } else if (Helper.isNetworkAvailable(getApplicationContext())) {
            showInputDialog();
        }
    }

    @Override
    public void eventCarentLocation() {
        LocationManager locationManager = (LocationManager) getApplication()
                .getSystemService(LOCATION_SERVICE);
        carentLOcationRefresh(locationManager);
    }
    public class HeadActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            ViewPager();
        }
    }

    private void ViewPager() {
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return hourlyWeatherFragment;
                } else if (position == 1) {
                    return dailyWeatherFragment;
                }
 else if(position == 2){
                return favoriteLocationWeatherFragment;
                }
                else return null;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) return "HOURLY";
                else if (position == 1) return "7 DAY";
                else if (position == 2) return "FAVORITES";
                else return null;
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }
    //-------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------



    private void updateWeatherData(final String tempCity, final double tempLat, final double tempLon, final String tempForecastUrl) {
        new Thread() {
                public void run() {
                if (Helper.isNetworkAvailable(getApplicationContext())) {
                    final String[] forecastUrl = new String[3];
                    String apiKey = "ddec71381c5621cdddefb5c58581e5bc";
                    Log.e("INTERNALLL: ", tempCity);
                    Log.e("INTERNALLLL toooooo : ", tempForecastUrl);
                    if (tempForecastUrl == BASE_CURRENT_WEATHER_URL_CITY) {
                        try {
                            forecastUrl[0] = RemoteFetch.getCurrent(getApplicationContext(), (new URL(String.format(BASE_CURRENT_WEATHER_URL_CITY, tempCity, apiKey))));
                            forecastUrl[1] = RemoteFetch.getDaily(getApplicationContext(), (new URL(String.format(BASE_DAILY_FORECAST_URL_CITY, tempCity, apiKey))));
                            forecastUrl[2] = RemoteFetch.getHourly(getApplicationContext(), (new URL(String.format(BASE_HOURLY_FORECAST_URL_CITY, tempCity, apiKey))));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            forecastUrl[0] = RemoteFetch.getCurrent(getApplicationContext(), (new URL(String.format(BASE_CURRENT_WEATHER_URL_COORD, tempLat, tempLon, apiKey))));
                            forecastUrl[1] = RemoteFetch.getDaily(getApplicationContext(), (new URL(String.format(BASE_DAILY_FORECAST_URL_COORD, tempLat, tempLon, apiKey))));
                            forecastUrl[2] = RemoteFetch.getHourly(getApplicationContext(), (new URL(String.format(BASE_HOURLY_FORECAST_URL_COORD, tempLat, tempLon, apiKey))));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("INTERNAL yetttttttttt: ", tempForecastUrl);
                    if ((forecastUrl[0] == null) && (Helper.isNetworkAvailable(getApplicationContext()))) {
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        getApplicationContext().getString(R.string.place_not_found),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if ((forecastUrl[0] != null) && (Helper.isNetworkAvailable(getApplicationContext()))) {
                        handler.post(new Runnable() {
                            public void run() {

                                try {
                                    mForecast.setCurrent(JSONWeatherParser.getWeather(forecastUrl[0]));
                                    Log.e("CURRR", forecastUrl[0]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    mForecast.setDailyForecast(JSONWeatherParser.getDailyForecast(forecastUrl[1]));
                                    Log.e("DAYYYYYY", forecastUrl[1]);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    mForecast.setHourlyForecast(JSONWeatherParser.getHourlyForecast(forecastUrl[2]));
                                    Log.e("HOURRRRR", forecastUrl[2]);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ForecastTransction.setForecast(mForecast);
                                storeObject(mForecast, getApplicationContext());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateDisplay();
                                    }
                                });
                            }
                        });
                    } else {
                        alertUserAboutError();
                    }
                } else if (!Helper.isNetworkAvailable(getApplicationContext())) {
                    handler.post(new Runnable() {
                        public void run() {
                            if (getObject(getApplicationContext()) == null) {
                                mEmptyTextView.setVisibility(View.VISIBLE);
                                Toast.makeText(MainActivity.this,
                                        getApplicationContext().getString(R.string.no_internet_connetion),
                                        Toast.LENGTH_LONG).show();
                                Log.e("updNul: ", "updNul");
                                ForecastTransction.setForecast(getObject(getApplicationContext()));
                            } else {
                                mForecast = getObject(getApplicationContext());
                                updateDisplay();
                                ForecastTransction.setForecast(mForecast);
                                Toast.makeText(MainActivity.this,
                                        getApplicationContext().getString(R.string.no_internet_connetion),
                                        Toast.LENGTH_LONG).show();
                                Log.e("updNoNul: ", "updNoNul");
                            }
                        }
                    });
                } else {
                    alertUserAboutError();
                }
            }
        }.start();
    }



    public void toggleRefresh() {
        final LocationManager locationManager = (LocationManager) getApplication()
                .getSystemService(LOCATION_SERVICE);
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    if ((nowURL == BASE_CURRENT_WEATHER_URL_COORD) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        coord = mHelper.CoordTracker(getApplicationContext());
                        updateWeatherData(city, coord[0], coord[1], nowURL);
                        new CityPreference(MainActivity.this).setLat(coord[0]);
                        new CityPreference(MainActivity.this).setLon(coord[1]);
                    } else {
                        //do nothing
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mRefreshImageView.setVisibility(View.VISIBLE);
                }
            };
        }
    }

    protected void updateDisplay() {
        mEmptyTextView.setVisibility(View.INVISIBLE);
        CurrentlyWeather currentlyWeather = mForecast.getCurrent();
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

    public static void storeObject(Forecast forecast, Context ctx) {
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "object_prefs", 0);
        complexPreferences.putObject("object_value", forecast);
        complexPreferences.commit();
    }

    public static Forecast getObject(Context ctx) {
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(ctx, "object_prefs", 0);
        Forecast forecast = complexPreferences.getObject("object_value", Forecast.class);
        if (forecast == null) {
            Log.e("Nul: ", "Nul");
            return null;
        } else {
            Log.e("NoNul: ", "NoNul");
            return forecast;
        }
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change city");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
                city = input.getText().toString();

            }
        });
        builder.show();
    }

    public void changeCity(String citySH) {
        nowURL = BASE_CURRENT_WEATHER_URL_CITY;
        updateWeatherData(citySH, coord[0], coord[1], nowURL);
        HeadActivityTask headActivityTask2 = new HeadActivityTask();
        headActivityTask2.execute();
        new CityPreference(this).setCity(citySH);
        new CityPreference(this).setNowURL(nowURL);
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    //-------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------------------------------------------------------------------
    private void inithializeComponent() {
        mRefreshImageView = (ImageView) findViewById(R.id.refreshImageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        cityField = (TextView) findViewById(R.id.city_field);
        updatedField = (TextView) findViewById(R.id.updated_field);
        detailsField = (TextView) findViewById(R.id.details_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
        descriptonField = (TextView) findViewById(R.id.decription_field);
        iconView = (ImageView) findViewById(R.id.icon_Image);
        Log.e("TESTING", "Lat = : " + coord[0]);
        Log.e("TESTING", "Lon = " + coord[1]);
        ibLogin = (ImageButton) findViewById(R.id.ibLogin);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_main);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        mAuth = FirebaseAuth.getInstance();
        profile = Profile.getCurrentProfile();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.e("TAG", "onAuthStateChanged:signed_in:" + user.getUid());
                    uid = user.getUid();
                    profile = Profile.getCurrentProfile();
                    if (profile != null)
                        presenter.loginFacebook(profile, user.getUid(), getApplicationContext());
                    Log.e("TAG", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.e("TAG", "onAuthStateChanged:signed_out");
                }
            }
        };
        inithializeComponent();
        toggleRefresh();
        presenter = new MainPresenterImplement(this);
        users = dbHelper.retriveUserFromRealm(this);
        Log.e(TAG, "wwwwwwwwwwwwwwwwwwwwww" + users.getUserName());
        onCreareToolBar();
        ibLogin.setOnClickListener(this);
        mCallbackManager = new CallbackManager.Factory().create();
        mRefreshImageView.setOnClickListener(this);
        InitializeDialog();
        LoginProgress loginProgress = new LoginProgress();
        loginProgress.execute();
        LocationManager locationManager;
//-------------------------------------------------------------------------------------------------------------------

        mRefreshImageView = (ImageView) findViewById(R.id.refreshImageView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        cityField = (TextView) findViewById(R.id.city_field);
        updatedField = (TextView) findViewById(R.id.updated_field);
        detailsField = (TextView) findViewById(R.id.details_field);
        currentTemperatureField = (TextView) findViewById(R.id.current_temperature_field);
        descriptonField = (TextView) findViewById(R.id.decription_field);
        iconView = (ImageView) findViewById(R.id.icon_Image);
        mEmptyTextView = (TextView) findViewById(R.id.tvEmpty);
        mEmptyTextView.setVisibility(View.INVISIBLE);
        Log.e("TESTTTTT: ", new CityPreference(MainActivity.this).getCity());
        Log.e("TESTTTTT toooo : ", new CityPreference(MainActivity.this).getNowURL());
        coord = mHelper.CoordTracker(getApplicationContext());
        Log.e("corddddddddd : ", coord[0] + "  " + coord[1]);
        updateWeatherData(new CityPreference(MainActivity.this).getCity(), new CityPreference(MainActivity.this).getLat(),
                new CityPreference(MainActivity.this).getLon(), new CityPreference(MainActivity.this).getNowURL());
        mRefreshImageView.setVisibility(View.VISIBLE);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.pager);
        locationManager = (LocationManager) getApplication()
                .getSystemService(LOCATION_SERVICE);
        ;
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (coord[0] == 0) {

                showSettingsAlert();
            }
        } else {
            updateWeatherData(new CityPreference(MainActivity.this).getCity(), coord[0],
                    coord[1], new CityPreference(MainActivity.this).getNowURL());
        }
        mt = new HeadActivityTask();
        mt.execute();
    }

    @Override

    protected void onStart() {
        super.onStart();
        setVisibleLoginItem();
        mAuth.addAuthStateListener(mAuthListener);
        //-------------------------------------------------------------------------------------------------------------------
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            if (requestCode == REGISTER_REQUEST) {
                users.setUserName(data.getStringExtra("UserName"));
                Log.e(TAG, "qqqqqqqqqqqqqqqq" + users.getUserName());
                users.setEmailAdress(data.getStringExtra("EmailADress"));
                users.setPhoto(RealmDbHelper.decodeBase64(data.getByteArrayExtra("PhotoUser")));
                ArrayList<String> loc = data.getStringArrayListExtra("UserLocation");
                modelLocation.setLocation(loc);
                users.setLocation(modelLocation);
                onCreareToolBar();
            }
        //-------------------------------------------------------------------------------------------------------------------

    }

    @Override
    protected void onPause() {
        super.onPause();
        RealmDbHelper dbHelper = new RealmDbHelper();
        ModelUser u = new ModelUser();
        u = dbHelper.retriveUserFromRealm(this);
        if (u.getUserName() != null && u.getUserName().length() > 0) {
            if (users.getUserName() != null && users.getUserName().length() > 0) {
                dbHelper.deleteUserFromRealm(this);
                dbHelper.saveUserToRealm(users, this);
            }
        }
        //-------------------------------------------------------------------------------------------------------------------
        storeObject(mForecast, getApplicationContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        //-------------------------------------------------------------------------------------------------------------------
        storeObject(mForecast, getApplicationContext());
    }

    private void loginFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.e("LOGIN", "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w("LOGIN", "signInWithEmail:failed", task.getException());
                            Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void InitializeDialog() {
        d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.login_layout);
        loginButtonFacebook = (LoginButton) d.findViewById(R.id.login_button);
        etLogin = (EditText) d.findViewById(R.id.etLogin);
        etPassword = (EditText) d.findViewById(R.id.etPassword);
        checkBoxShowPassword = (CheckBox) d.findViewById(R.id.checkBoxShowPassword);
        inputType = etPassword.getInputType();
    }

    private void onCreareToolBar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        FirebaseHelper.modelUser = users;
        onCreateNavigationDraver();
    }

    private void onCreateNavigationDraver() {
        NavigationDraverFragment draverFragment = (NavigationDraverFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_draver);
        draverFragment.setUp(R.id.fragment_navigation_draver, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar, users, uid);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LocationManager locationManager = (LocationManager) getApplication()
                .getSystemService(LOCATION_SERVICE);
        if (item.getItemId() == R.id.change_city) {
            if (!Helper.isNetworkAvailable(getApplicationContext())) {
                Toast.makeText(MainActivity.this,
                        getApplicationContext().getString(R.string.no_internet_connetion),
                        Toast.LENGTH_LONG).show();
            } else if (Helper.isNetworkAvailable(getApplicationContext())) {
                showInputDialog();
            }
        } else if (item.getItemId() == R.id.current_location) {
            carentLOcationRefresh(locationManager);
        } else if (item.getItemId() == R.id.exit) {
            finish();
        } else if (item.getItemId() == R.id.refresh) {
            if ((nowURL == BASE_CURRENT_WEATHER_URL_COORD) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                coord = mHelper.CoordTracker(getApplicationContext());
                updateWeatherData(city, coord[0], coord[1], nowURL);
                new CityPreference(this).setLat(coord[0]);
                new CityPreference(this).setLon(coord[1]);
            } else {
                //do nothing
            }
        }
        return false;
    }

    private void carentLOcationRefresh(LocationManager locationManager) {
        if (!Helper.isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(MainActivity.this,
                    getApplicationContext().getString(R.string.no_internet_connetion),
                    Toast.LENGTH_LONG).show();
        } else if (Helper.isNetworkAvailable(getApplicationContext()) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            nowURL = BASE_CURRENT_WEATHER_URL_COORD;
            coord = mHelper.CoordTracker(getApplicationContext());
            Log.e("locccccccc : ", coord[0] + " " + coord[1]);
            updateWeatherData(city, coord[0], coord[1], nowURL);
            new CityPreference(this).setNowURL(nowURL);
            new CityPreference(this).setLat(coord[0]);
            new CityPreference(this).setLon(coord[1]);
            HeadActivityTask headActivityTask = new HeadActivityTask();
            headActivityTask.execute();
        }
    }


    private void showDialogLogin() {
        loginButtonFacebook.setReadPermissions("email", "public_profile");
        loginButtonFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                handleFacebookAccessToken(loginResult.getAccessToken());
                Log.e(TAG, "SsssssignInnn");
                if (mAuth.getCurrentUser() != null)
                    presenter.loginFacebook(profile, mAuth.getCurrentUser().getUid(), getApplicationContext());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        progressBar = (ProgressBar) d.findViewById(R.id.progressBarLogin);
        imOk = (ImageView) d.findViewById(R.id.iv_ok_login);
        etRegister = (TextView) d.findViewById(R.id.etRegister);
        etRegister.setSelectAllOnFocus(true);

        checkBoxShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxShowPassword.isChecked()) {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    etPassword.setInputType(inputType);
                }
            }
        });
        etRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.cancel();
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER_REQUEST);
            }
        });

        imOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Helper.isNetworkAvailable(getApplicationContext())) {
                    d.cancel();
                    Toast.makeText(getApplicationContext(),
                            "No internet access", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    presenter.retriveUserFirebase(etLogin.getText().toString(), etPassword.getText().toString(), getApplicationContext());
                }
            }
        });
        d.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibLogin:
                if (this.users.getUserName() != null) {
                    showDialogQuit();
                } else {
                    showDialogLogin();
                }
                break;
            case R.id.currentLoc:
                break;
            case R.id.refreshImageView:
                toggleRefresh();
                break;
            case R.id.cityLoc:
                break;
            default:
                break;
        }
    }

    private void showDialogQuit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quit");
        final TextView input = new TextView(this);
        input.setText("You really want to continue?");
        builder.setView(input);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String temp = users.getEmailAdress();
                FirebaseAuth.getInstance().signOut();
                if (users.getEmailAdress() == null || temp.length() == 0 || temp.equals("")) {
                    loginButtonFacebook.performClick();
                }
                ModelUser userQuit = new ModelUser();
                ModelLocation locationQuit = new ModelLocation();
                userQuit.setLocation(locationQuit);
                users = userQuit;
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                dbHelper.deleteUserFromRealm(getApplication());
                onCreareToolBar();
                setVisibleLoginItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // n pressing the Settings button.
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MainActivity.this.startActivity(intent);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public void dialogClosed() {
        d.cancel();
    }

    @Override
    public void setLoginError(String value) {
        etLogin.setError(value);
    }

    @Override
    public void setPasswordError() {
        etPassword.setError(getString(R.string.password_error));
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setUser(ModelUser activeUser) {
        this.users = FirebaseHelper.modelUser;
        Log.e(TAG, "eeeeeeeeeeeeeeeeee" + users.getUserName());
        this.users.setLocation(FirebaseHelper.modelLocation);
        etLogin.setText("");
        etPassword.setText("");
        mProgressBar.setVisibility(View.INVISIBLE);
        ibLogin.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_outline_white_24dp));
        onCreareToolBar();
    }

    @Override
    public void setDialogClosed() {
        dialogClosed();
    }

    @Override
    public void loginUserFirebase(String userLogin, String userPassword) {
        loginFirebase(userLogin, userPassword);
        LoginProgress loginProgress = new LoginProgress();
        loginProgress.execute();
    }


    private void setVisibleLoginItem() {
        if (users.getUserName() == null) {
            ibLogin.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_open_white_24dp));
        } else {
            ibLogin.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_outline_white_24dp));
        }
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private class LoginProgress extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                presenter.loginFacebook(profile, mAuth.getCurrentUser().getUid(), getApplicationContext());
                firebaseHelper.retrivDataUser(mAuth.getCurrentUser().getUid());
                firebaseHelper.downloadPhotoStorage(mAuth.getCurrentUser().getUid());
                uid = mAuth.getCurrentUser().getUid();
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressBar.setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
            if (mAuth.getCurrentUser() != null) {
                users = FirebaseHelper.modelUser;
                Log.e(TAG, "tttttttttttttttttttt" + users.getUserName());
                users.setPhoto(FirebaseHelper.photoDownload);

                ModelUser u = new ModelUser();
                u = dbHelper.retriveUserFromRealm(getApplicationContext());
                if (u.getUserName() != null && u.getUserName().length() > 0) {
                    if (users.getUserName() != null && users.getUserName().length() > 0) {
                        dbHelper.deleteUserFromRealm(getApplicationContext());
                    }
                }
                dbHelper.saveUserToRealm(users, getApplicationContext());
            }
            onCreareToolBar();
            d.cancel();
        }
    }


}