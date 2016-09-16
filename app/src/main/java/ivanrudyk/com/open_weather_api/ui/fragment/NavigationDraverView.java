package ivanrudyk.com.open_weather_api.ui.fragment;

import ivanrudyk.com.open_weather_api.model.ModelUser;

/**
 * Created by Ivan on 10.08.2016.
 */
public interface NavigationDraverView {
    void setUpFragment();

    void setLocationAddError(String s);

    void setDialogClosed(String parametrProgress);

    void showProgress(String param);

    void hideProgress(String parametrProgress);

    void setUser(ModelUser user);
}
