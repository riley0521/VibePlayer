plugins {
    alias(libs.plugins.vibeplayer.android.library)
    alias(libs.plugins.vibeplayer.android.instrumented.testing)
    alias(libs.plugins.vibeplayer.room)
    alias(libs.plugins.vibeplayer.koin)
}

android {
    namespace = "com.rfcoding.vibeplayer.core.database"
}

dependencies {
    implementation(projects.core.domain)
}
