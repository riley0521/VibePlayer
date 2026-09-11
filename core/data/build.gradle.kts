plugins {
    alias(libs.plugins.vibeplayer.android.library)
    alias(libs.plugins.vibeplayer.koin)
}

android {
    namespace = "com.rfcoding.vibeplayer.core.data"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.database)
}
