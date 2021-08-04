package svcs

import java.io.File
import java.nio.file.Path
import kotlin.io.path.writeLines

class Svcs {
    val config: File
    val index: File
    var username: String
    val files = mutableListOf<String>()

    init {
        val vcsDir = File("vsc")
        if (!vcsDir.exists()) {
            vcsDir.mkdir()
        }
        config = File(vcsDir, "config.txt")
        index = File(vcsDir, "index.txt")
        username = if (config.exists()) config.readText() else ""
        if (index.exists()) {
            files.addAll(index.readLines())
        }
        System.err.println("""
            username=$username
            files=$files
        """.trimIndent())
    }

    fun config(args: Array<String>) {
        if (args.isEmpty()) {
            if (username.isEmpty()) {
                println("Please, tell me who you are.")
                return
            }
        } else {
            username = args[0]
            config.writeText(username)
        }
        println("The username is $username.")
    }

    fun add(args: Array<String>) {
        if (args.isEmpty()) {
            if (files.isEmpty()) {
                println("Add a file to the index.")
            } else {
                println("Tracked files:")
                files.forEach({ file -> println(file) })
            }
        } else {
            val filename = args[0]
            val file = File(filename)
            if (file.exists()) {
                files.add(file.name)
                index.toPath().writeLines(files)
//                index.writeText(files.joinToString(System.out.linese))
                println("The file '${file.name}' is tracked.")
            } else {
                println("Can't find '$filename'.")
            }
        }
    }
}