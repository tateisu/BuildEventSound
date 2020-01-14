import com.intellij.openapi.diagnostic.Logger
import org.apache.log4j.Level
import org.junit.Test


class TestBuildEvent {

    companion object {
        init {
            LogCategory.dst = object : Logger() {

                override fun isDebugEnabled(): Boolean = true

                override fun setLevel(level: Level?) {
                }

                override fun warn(message: String?, t: Throwable?) {
                    println("warn: $message")
                    t?.printStackTrace()
                }


                override fun info(message: String?) {
                    println("info: $message")
                }

                override fun info(message: String?, t: Throwable?) {
                    println("info: $message")
                    t?.printStackTrace()
                }

                override fun error(message: String?, t: Throwable?, vararg details: String?) {
                    if (details.isNotEmpty()) throw NotImplementedError("Logger.error() with details is not implemented")
                    println("error: $message")
                    t?.printStackTrace()
                }


                override fun debug(message: String?) {
                    println("debug: $message")
                }

                override fun debug(t: Throwable?) {
                    t?.printStackTrace()
                }

                override fun debug(message: String?, t: Throwable?) {
                    println("debug: $message")
                    t?.printStackTrace()
                }

            }
        }
    }

    @Test
    fun test1() {
        println("(test1")
        QueuedCommandRunner.handleEvent(BuildEvent.BeforeCompile, 3000L)
        println(")test1")
    }
}
