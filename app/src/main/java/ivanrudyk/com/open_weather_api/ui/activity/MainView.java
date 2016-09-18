package ivanrudyk.com.open_weather_api.ui.activity;


import ivanrudyk.com.open_weather_api.model.ModelUser;

public interface MainView {
    void setLoginError(String userName);

    void hideProgress();

    void setPasswordError();

    void showProgress();

    void setUser(ModelUser activeUser);

    void setDialogClosed();

    void loginUserFirebase(String userLogin, String userPassword);

    void setFavoriteLocatinActivity(String s);
}
