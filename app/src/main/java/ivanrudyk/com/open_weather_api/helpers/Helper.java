package ivanrudyk.com.open_weather_api.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * Created by Yulian on 14.08.2016.
 */
public class Helper {

    public Helper() {
    }
    GPSTracker gps;
    public static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting())
        {
            return true;
        }
        return false;
    }
    public double[] CoordTracker(Context context) {
        gps = new GPSTracker(context);
        double [] coord = new double[2];
        if(gps.canGetLocation()) {
            coord[0] = gps.getLatitude();
            coord[1] = gps.getLongitude();
            Log.e("Tloooooooooc : " , gps.canGetLocation() + "");
            Log.e("gpscorrdd : " , coord[0] + "  " + coord[1]);
        } else {
//            gps.showSettingsAlert();
            Log.e("Tloooooooooc : " , gps.canGetLocation() + "");
        }
        return coord;
    }
    public void toggleRefresh(ProgressBar mProgressBar , ImageView mRefreshImageView) {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... voids) {

                    return null;
                }
            };
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }
}
