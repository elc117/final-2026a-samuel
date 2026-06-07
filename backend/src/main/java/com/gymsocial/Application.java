package com.gymsocial;

import com.gymsocial.config.ApplicationConfig;
import com.gymsocial.config.ApplicationModule;

public final class Application {

    private Application() {
    }

    public static void main(String[] args) {
        ApplicationConfig appConfig = ApplicationConfig.fromEnvironment();
        ApplicationModule.create(appConfig).start(appConfig.port());
    }
}
