import com.intellij.openapi.diagnostic.Logger

class LogCategory(private val name: String) {
    companion object {
        val dst = Logger.getInstance("jp.juggler.BuildEventSound")
    }

    fun e(exArg: Throwable?, msg: String) {
        var ex: Throwable? = exArg
        while (ex != null) {
            dst.error("$name : $msg", ex)
            ex = ex.cause
        }
    }

    fun w(msg: String) :Unit{
        dst.warn(msg)
    }

    fun i(msg: String) {
        dst.info("$name : $msg")
    }

}