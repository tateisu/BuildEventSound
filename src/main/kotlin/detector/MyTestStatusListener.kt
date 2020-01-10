package detector

import BuildEvent
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener

class MyTestStatusListener : TestStatusListener() {
    override fun testSuiteFinished(root: AbstractTestProxy?) {
        when (root?.isPassed) {
            null -> return
            true -> BuildEvent.TestPassed
            else -> BuildEvent.TestDefect
        }.dispatch()
    }
}
