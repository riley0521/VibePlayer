package com.rfcoding.vibeplayer.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * JUnit4 + AndroidJUnit4 + Room's MigrationTestHelper, for the modules that carry instrumented tests.
 * The Android runner can't run JUnit5, so these tests don't share [configureUnitTesting]'s stack, and
 * the dependencies land on `androidTestImplementation` only.
 */
internal fun Project.configureInstrumentedTesting() {
    extensions.configure<LibraryExtension> {
        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    dependencies {
        "androidTestImplementation"(libs.bundle("instrumented-testing"))
    }
}
