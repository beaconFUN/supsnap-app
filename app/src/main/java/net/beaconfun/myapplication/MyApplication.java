package net.beaconfun.myapplication;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by akiya on 2017/10/09.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfiguration);
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
