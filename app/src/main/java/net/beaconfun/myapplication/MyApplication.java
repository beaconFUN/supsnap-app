package net.beaconfun.myapplication;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by akiya on 2017/10/09.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
