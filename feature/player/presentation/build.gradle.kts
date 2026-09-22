plugins {
    alias(libs.plugins.vibeplayer.android.feature)
}

android {
    namespace = "com.rfcoding.vibeplayer.feature.player.presentation"
}

dependencies {
    // rememberLauncherForActivityResult, for the storage permission that saving a card needs on API 28.
    implementation(libs.androidx.activity.compose)
    // Drag-to-reorder for the Queue sheet, as on the Edit playlist screen.
    implementation(libs.reorderable)

    testImplementation(projects.core.testing)
}
