import com.rfcoding.vibeplayer.convention.configureKotlinJvm
import com.rfcoding.vibeplayer.convention.configureUnitTesting
import com.rfcoding.vibeplayer.convention.libs
import com.rfcoding.vibeplayer.convention.library
import com.rfcoding.vibeplayer.convention.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Pure Kotlin/JVM module without any Android dependency.
 */
class DomainModuleConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("java-library")
            pluginManager.apply(libs.pluginId("kotlin-jvm"))

            configureKotlinJvm()
            configureUnitTesting()

            dependencies {
                "implementation"(libs.library("kotlinx-coroutines-core"))
            }

            // Android modules run their unit tests with `testDebugUnitTest`. This alias makes the
            // root `./gradlew testDebugUnitTest` run the tests of pure Kotlin modules too.
            tasks.register("testDebugUnitTest") {
                group = "verification"
                description = "Runs the unit tests of this JVM module (alias for `test`)."
                dependsOn("test")
            }
        }
    }
}
