import com.intellij.openapi.command.CommandEvent
import com.intellij.openapi.command.CommandListener
import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompilerTopics
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTask
import com.intellij.task.ProjectTaskContext
import com.intellij.task.ProjectTaskListener
import com.intellij.task.ProjectTaskResult

open class Subscribe(private val project: Project) {

    companion object{
        val log = LogCategory("Subscribe")
    }

    private val messages by lazy { project.messageBus.connect() }

    init {
        log.i("init.")
        messages.subscribe(CompilerTopics.COMPILATION_STATUS, object : CompilationStatusListener {
            override fun compilationFinished(aborted: Boolean, errors: Int, warnings: Int, compileContext: CompileContext) {
                log.i("compilationFinished.")
                Dispatcher.dispatch(
                    when {
                        errors > 0 -> Events.BuildError
                        warnings > 0 -> Events.BuildWarning
                        else -> Events.BuildSuccess
                    }
                )
            }
        })
    }
}

