package bobko.email.todo

fun String.ellipsis(n: Int): String {
    return take(n) + if (length > n) "..." else ""
}
