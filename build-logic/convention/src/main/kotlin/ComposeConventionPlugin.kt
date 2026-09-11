import com.android.build.api.dsl.CommonExtension
import com.rfcoding.vibeplayer.convention.bundle
import com.rfcoding.vibeplayer.convention.libs
import com.rfcoding.vibeplayer.convention.library
import com.rfcoding.vibeplayer.convention.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Compose compiler + BOM + the common Compose libraries. Apply after an Android plugin.
 */
class ComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-compose"))

            extensions.getByType<CommonExtension>().buildFeatures.compose = true

            dependencies {
                val bom = platform(libs.library("androidx-compose-bom"))
                "implementation"(bom)
                "implementation"(libs.bundle("compose"))
                "debugImplementation"(libs.bundle("compose-debug"))
            }
        }
    }
}
