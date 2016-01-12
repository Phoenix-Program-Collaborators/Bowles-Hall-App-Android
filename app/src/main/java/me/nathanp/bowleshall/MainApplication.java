package me.nathanp.bowleshall;

import android.app.Application;

import com.firebase.client.Firebase;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
