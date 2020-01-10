fun String?.notEmpty(): String? =
    if (this?.isNotEmpty() == true) this else null

fun Throwable.formatCaption(caption: String) =
    "$caption ${javaClass.simpleName} $message".trim()
