import com.rfcoding.vibeplayer.convention.configureInstrumentedTesting
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Opt-in, unlike `vibeplayer.android.library`'s unit testing: only the modules that actually have an
 * `androidTest` source set pull in androidx.test. Apply it after the android-library plugin, whose
 * extension it configures.
 */
class AndroidInstrumentedTestingConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            configureInstrumentedTesting()
        }
    }
}
