package com.example.xyzreader;

import android.app.Application;

import com.example.xyzreader.logger.TimberLogImplementation;

/**
 * Created by Antonio Vitiello on 22/06/2018.
 */
public class XyzApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Timber initialization
        TimberLogImplementation.init(getString(R.string.app_name));
    }

}
