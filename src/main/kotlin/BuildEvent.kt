enum class BuildEvent {
    BeforeCompile,
    BuildSuccess,
    BuildError,
    BuildWarning,
    TestPassed,
    TestDefect,

    ;

    fun dispatch() = QueuedCommandRunner.post(this)
}
