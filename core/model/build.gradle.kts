plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.chalkak.recap.core.model"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
