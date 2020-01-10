package detector

import BuildEvent
import LogCategory
import com.android.tools.idea.gradle.project.build.GradleBuildContext
import com.android.tools.idea.project.AndroidProjectBuildNotifications
import com.intellij.openapi.project.Project
import com.android.ide.common.blame.Message

class SubscribeAndroid(project: Project){

    companion object {
        private val log = LogCategory("SubscribeAndroid")
    }

    init {
        AndroidProjectBuildNotifications.subscribe(project) { ctx ->
            log.i("AndroidProjectBuildNotifications ctx=$ctx")
            (ctx as? GradleBuildContext)?.buildResult?.run {
                when {
                    isBuildSuccessful -> BuildEvent.BuildSuccess
                    getCompilerMessages(Message.Kind.WARNING).isNotEmpty() -> BuildEvent.BuildWarning
                    else -> BuildEvent.BuildError
                }.dispatch()
            }
        }
    }
}
