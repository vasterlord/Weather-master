package ivanrudyk.com.open_weather_api.ui.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ivanrudyk.com.open_weather_api.R;
import ivanrudyk.com.open_weather_api.adapter.FavoritesLocationAdapter;
import ivanrudyk.com.open_weather_api.helpers.FirebaseHelper;
import ivanrudyk.com.open_weather_api.helpers.PhotoHelper;
import ivanrudyk.com.open_weather_api.model.FavoriteLocationWeather;
import ivanrudyk.com.open_weather_api.model.ModelUser;
import ivanrudyk.com.open_weather_api.presenter.fragment.NavigationDraverPresenterImplement;
import ivanrudyk.com.open_weather_api.presenter.fragment.NavigatonDraverPresenter;


/**
 * A simple {@link Fragment} subclass.
 */

public class NavigationDraverFragment extends Fragment implements NavigationDraverView {

    public interface onSomeEventListenerDraver {
        void eventMapsOpen(String s);

        void eventChangeSity();

        void eventCarentLocation();
    }

    private onSomeEventListenerDraver someEventListener;



    public static final String PREF_FILE_NAME = "preffilename";
    public static final String KEY_USER_LEARNED_DRAWER = "user_learned_drawer";
    public ModelUser users = new ModelUser();
    TextView tvNavUserName, tvNavLogin;
    ImageView ivPhotoUser;
    ListView lvLocation;
    ImageView bAdd;
    Button bAddLocation;
    EditText etAddLocation;

    private ProgressBar progressBar;
    LinearLayout linearLayoutAddLoc, linearLayoutCarentLocation, linearLayoutChangeCity;
    PhotoHelper photoHelper = new PhotoHelper();
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private LinearLayout linearLayoutMapsOpen;
    private boolean mUserLearndDrawer;
    private boolean mFromSavedInstanseState;
    private View containerView;
    private TextView tvDeleteLocation;
    private ProgressBar prBarDeleteDialogProgBar;
    private Button bOkDeleteLoc;
    private Button bCanselDeleteLoc;

    String uid;
    private FavoritesLocationAdapter locationAdapter;

    private Dialog dialogAdd;
    private Dialog dialogDelete;

    NavigatonDraverPresenter draverPresenter;


    public NavigationDraverFragment() {
    }

    public static void saveToPreferenses(Context context, String preferenceName, String preferenceValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(preferenceName, preferenceValue);
        editor.apply();
    }

    public static String readFromPreferenses(Context context, String preferenceName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(preferenceName, defaultValue);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (onSomeEventListenerDraver) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_navigation_draver, container, false);
        initializeFragment(v);


        return v;
    }

    private void initializeFragment(View v) {
        ivPhotoUser = (ImageView) v.findViewById(R.id.ivPhotoUser);
        tvNavUserName = (TextView) v.findViewById(R.id.tvDrUserName);
        tvNavLogin = (TextView) v.findViewById(R.id.tvDrLogin);
        lvLocation = (ListView) v.findViewById(R.id.listViewLocation);
        linearLayoutMapsOpen = (LinearLayout) v.findViewById(R.id.linearLayoutMaps);
        bAdd = (ImageView) v.findViewById(R.id.ivAddLocation);
        linearLayoutAddLoc = (LinearLayout) v.findViewById(R.id.linLayoutAddLoc);
        linearLayoutCarentLocation = (LinearLayout) v.findViewById(R.id.linearLayoutCarentLoc);
        linearLayoutChangeCity = (LinearLayout) v.findViewById(R.id.linearLayoutChangeCity);
        draverPresenter = new NavigationDraverPresenterImplement(this);
        linearLayoutAddLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (users.getUserName() != null) {
                    dialogAdd = new Dialog(getActivity());
                    dialogAdd.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogAdd.setContentView(R.layout.add_location_layout);
                    etAddLocation = (EditText) dialogAdd.findViewById(R.id.etAddLocation);
                    bAddLocation = (Button) dialogAdd.findViewById(R.id.bAddLocation);
                    progressBar = (ProgressBar) dialogAdd.findViewById(R.id.progressBarAddLocation);

                    bAddLocation.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            draverPresenter.addLocation(users, uid, etAddLocation.getText().toString());
                        }
                    });
                    dialogAdd.show();
                }
            }
        });
        linearLayoutCarentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                someEventListener.eventCarentLocation();  
            }
        });
        linearLayoutMapsOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                someEventListener.eventMapsOpen("OK");
            }
        });
        linearLayoutChangeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                someEventListener.eventChangeSity();
            }
        });
        arrayAdapter();
    }

    private void deleteDialogOpen(final ModelUser users, final String uid, final int position){
        dialogDelete = new Dialog(getContext());
        dialogDelete.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogDelete.setContentView(R.layout.delete_location_layout);
        tvDeleteLocation = (TextView) dialogDelete.findViewById(R.id.tvDeleteLocationInfo);
        bOkDeleteLoc = (Button) dialogDelete.findViewById(R.id.bOkDeleteLocation);
        bCanselDeleteLoc = (Button) dialogDelete.findViewById(R.id.bCencelDeleteLocation);
        prBarDeleteDialogProgBar = (ProgressBar) dialogDelete.findViewById(R.id.progressBarAddLocation);
        tvDeleteLocation.setText("If you want to remove from favorite locations "+ FavoriteLocationWeather.listLocation.get(position)+"?");

        bOkDeleteLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                draverPresenter.deleteLocation(users, uid, position);
            }
        });
        bCanselDeleteLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogDelete.cancel();
            }
        });
        dialogDelete.show();
    }

    public void arrayAdapter() {
        locationAdapter = new FavoritesLocationAdapter(getActivity(), users.getLocation().getLocation());
        if (FavoriteLocationWeather.listLocation!=null && FavoriteLocationWeather.listLocation.size()>0) {
            String temp = FavoriteLocationWeather.listLocation.get(0);
                lvLocation.setAdapter(locationAdapter);
        }

        lvLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Log.e("LOG: ", "City = "+ FavoriteLocationWeather.listLocation.get(position));
                deleteDialogOpen(users, uid, position);
//                draverPresenter.deleteLocation(users, uid, position);
            }
        });
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolBar, ModelUser users, String uid) {
        this.users = users;
        this.uid = uid;
        tvNavUserName.setText(this.users.getUserName());
        tvNavLogin.setText(this.users.getEmailAdress());
        ivPhotoUser.setImageBitmap(PhotoHelper.getCircleMaskedBitmapUsingClip(this.users.getPhoto(), 60));
        arrayAdapter();


        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolBar, R.string.drawer_open, R.string.drawer_close) {


            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mUserLearndDrawer) {
                    mUserLearndDrawer = true;
                    //saveToPreferenses(getActivity(), KEY_USER_LEARNED_DRAWER, mUserLearndDrawer + "");
                }
                getActivity().invalidateOptionsMenu();
            }


            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }
        };

//        if (!mUserLearndDrawer && !mFromSavedInstanseState) {
//            mDrawerLayout.openDrawer(containerView);
//        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

    }

    @Override
    public void setUpFragment() {
        FavoritesLocationAdapter locationAdapter = new FavoritesLocationAdapter(this.getContext(), FirebaseHelper.modelLocation.getLocation());
        lvLocation.setAdapter(locationAdapter);
    }

    @Override
    public void setLocationAddError(String s) {
        etAddLocation.setError(s);
    }

    @Override
    public void setDialogClosed(String parametrProgress) {
        dialogClosed(parametrProgress);
    }

    @Override
    public void showProgress(String param) {
        if(param.equals("add")) {
            progressBar.setVisibility(View.VISIBLE);
        }
        else if(param.equals("delete")) {
            tvDeleteLocation.setVisibility(View.INVISIBLE);
            prBarDeleteDialogProgBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideProgress(String parametrProgress) {
        if(parametrProgress.equals("add")) {
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if(parametrProgress.equals("delete")) {
            tvDeleteLocation.setVisibility(View.VISIBLE);
            prBarDeleteDialogProgBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setUser(ModelUser user) {
        this.users = user;
    }

    private void dialogClosed(String parametrProgress) {
        if(parametrProgress.equals("add")) {
            dialogAdd.cancel();
            progressBar.setVisibility(View.INVISIBLE);
        }
        else if(parametrProgress.equals("delete")) {
            dialogDelete.cancel();
            tvDeleteLocation.setVisibility(View.VISIBLE);
            prBarDeleteDialogProgBar.setVisibility(View.INVISIBLE);
        }

    }
}
