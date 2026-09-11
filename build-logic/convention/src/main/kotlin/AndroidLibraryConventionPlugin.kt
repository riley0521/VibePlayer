import com.android.build.api.dsl.LibraryExtension
import com.rfcoding.vibeplayer.convention.configureKotlinAndroid
import com.rfcoding.vibeplayer.convention.configureUnitTesting
import com.rfcoding.vibeplayer.convention.libs
import com.rfcoding.vibeplayer.convention.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("android-library"))

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            configureUnitTesting()
        }
    }
}
