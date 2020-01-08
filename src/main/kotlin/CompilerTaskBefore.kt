import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileTask

class CompilerTaskBefore : CompileTask {
    override fun execute(context: CompileContext): Boolean {
        Dispatcher.dispatch(Events.BeforeCompile)
        return true
    }
}