import com.android.tools.idea.gradle.project.build.GradleBuildContext
import com.android.tools.idea.project.AndroidProjectBuildNotifications
import com.intellij.openapi.project.Project
import com.android.ide.common.blame.Message

class SubscribeAndroid(private val project: Project) : Subscribe(project) {

    companion object{
        val log = LogCategory("SubscribeAndroid")
    }
    init {
        log.i("init")
        AndroidProjectBuildNotifications.subscribe(project) { ctx ->
            log.i("AndroidProjectBuildNotifications ctx=$ctx")
            if (ctx is GradleBuildContext) ctx.buildResult.run {
                log.i("ctx.buildResult.run")
                Dispatcher.dispatch(
                    when {
                        isBuildSuccessful -> Events.BuildSuccess
                        getCompilerMessages(Message.Kind.WARNING).isNotEmpty() -> Events.BuildWarning
                        else -> Events.BuildError
                    }
                )
            }
        }
    }

}