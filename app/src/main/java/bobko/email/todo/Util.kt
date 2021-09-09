package bobko.email.todo

fun String.ellipsis(n: Int): String {
    return take(n).trim() + if (length > n) "..." else ""
}
