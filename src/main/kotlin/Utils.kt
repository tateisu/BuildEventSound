@file:Suppress("unused")

fun String?.notEmpty(): String? =
    if (this?.isNotEmpty() == true) this else null

fun Int?.notZero(): Int? =
    if (this != 0 ) this else null

fun Long?.notZero(): Long? =
    if (this != 0L ) this else null

fun Float?.notZero(): Float? =
    if (this != 0f ) this else null

fun Double?.notZero(): Double? =
    if (this != 0.0 ) this else null

fun Throwable.formatCaption(caption: String) =
    "$caption ${javaClass.simpleName} $message".trim()
