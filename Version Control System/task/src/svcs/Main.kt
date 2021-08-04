package svcs

fun main(args: Array<String>) {
    System.err.println("args=${args.joinToString(",")}")
    val svcs = Svcs()
    if (args.isEmpty() || args[0] == "--help") {
        printHelp()
        return
    }
    val arguments = args.sliceArray(1..args.lastIndex)
    System.err.println("arguments=${arguments.joinToString(",")}")
    when (val argument = args[0]) {
        "config" -> svcs.config(arguments)
        "add" -> svcs.add(arguments)
        "log" -> println("Show commit logs.")
        "commit" -> println("Save changes.")
        "checkout" -> println("Restore a file.")
        else -> println("'$argument' is not a SVCS command.")
    }
}

fun config(args: Array<String>) {
}

fun printHelp() {
    println("""
        These are SVCS commands:
        config     Get and set a username.
        add        Add a file to the index.
        log        Show commit logs.
        commit     Save changes.
        checkout   Restore a file.
    """.trimIndent())
}
