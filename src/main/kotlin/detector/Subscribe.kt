package detector

import BuildEvent
import LogCategory
import com.intellij.openapi.compiler.CompilationStatusListener
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompilerTopics
import com.intellij.openapi.project.Project

open class Subscribe(private val project: Project) {

    companion object {
        private val log = LogCategory("Subscribe")
    }

    init {
        log.i("init.")
        project.messageBus.connect().subscribe(
            CompilerTopics.COMPILATION_STATUS,
            object : CompilationStatusListener {
                override fun compilationFinished(
                    aborted: Boolean,
                    errors: Int,
                    warnings: Int,
                    compileContext: CompileContext
                ) {
                    log.i("compilationFinished.")
                    when {
                        errors > 0 -> BuildEvent.BuildError
                        warnings > 0 -> BuildEvent.BuildWarning
                        else -> BuildEvent.BuildSuccess
                    }.dispatch()

                }
            }
        )
    }
}
