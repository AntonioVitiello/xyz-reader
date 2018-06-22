package com.example.xyzreader.logger;

import timber.log.Timber;

/**
 * Created by Antonio Vitiello on 22/06/2018.
 */
public class TimberLogImplementation {
    public static void init(final String debugTag) {
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return String.format("[%1$s][%4$s.%2$s(%3$s)]",
                        debugTag,
                        element.getMethodName(),
                        element.getLineNumber(),
                        super.createStackElementTag(element));
            }
        });
    }
}
