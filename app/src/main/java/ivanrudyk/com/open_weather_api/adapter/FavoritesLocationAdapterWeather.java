package ivanrudyk.com.open_weather_api.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ivanrudyk.com.open_weather_api.R;
import ivanrudyk.com.open_weather_api.model.FavoriteLocationWeather;

/**
 * Created by Ivan on 19.08.2016.
 */
public class FavoritesLocationAdapterWeather extends  RecyclerView.Adapter<FavoritesLocationAdapterWeather.ViewHolder> {
    private Context context;
    private ArrayList<FavoriteLocationWeather> arrayListLocation = new ArrayList<>();



    public FavoritesLocationAdapterWeather(Context applicationContext, ArrayList<FavoriteLocationWeather> arrayListLocation) {
        this.context = applicationContext;
        this.arrayListLocation = arrayListLocation;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.favorite_lication_list_item, parent, false);
        ViewHolder holder  = new ViewHolder(view);
        Log.e("INTERNALLL: ", "Set Adapter favorite222");
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindFavorite(arrayListLocation.get(position));
    }


    @Override
    public int getItemCount() {
        return arrayListLocation.size();
    }
//    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvCity;
        TextView tvTemperarure;
        TextView tvSummary;
        public ImageView ivIconSummaryFavLoc;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCity = (TextView) itemView.findViewById(R.id.tvCityFl);
            tvSummary = (TextView) itemView.findViewById(R.id.tvSummaryFavLoc);
            tvTemperarure = (TextView) itemView.findViewById(R.id.tetvTperatureFavLoc);
            ivIconSummaryFavLoc = (ImageView) itemView.findViewById(R.id.ivImageViewFavorite);
        }

        public void bindFavorite(FavoriteLocationWeather favoritLocWeather) {
            Log.e("INTERNALLL: ", "Set Adapter favorite222"+favoritLocWeather.getSummary());
            tvSummary.setText(favoritLocWeather.getSummary());
            tvTemperarure.setText(String.format("%.0f",favoritLocWeather.getTemperature()) + "℃");
            tvCity.setText(favoritLocWeather.getCity());
        }

        @Override
        public void onClick(View view) {

        }
    }
}
