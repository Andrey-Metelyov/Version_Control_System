package svcs

import java.io.File
import java.security.MessageDigest
import kotlin.io.path.copyTo
import kotlin.io.path.fileSize
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeLines

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

class Svcs {
    val config: File
    val index: File
    val log: File
    val commitsDir: File
    var username: String
    val files = mutableListOf<String>()
    val commits = mutableListOf<Commit>()

    init {
        System.err.println("*****%*****#*****%*****")
        val vcsDir = File("vcs")
        if (!vcsDir.exists()) {
            System.err.println("create dir: ${vcsDir.absolutePath}")
            vcsDir.mkdir()
        }
        config = File(vcsDir, "config.txt")
        index = File(vcsDir, "index.txt")
        log = File(vcsDir, "log.txt")
        commitsDir = File(vcsDir, "commits")
        System.err.println("config: ${config.absolutePath}")
        System.err.println("index: ${index.absolutePath}")
        System.err.println("commits: ${commitsDir.absolutePath}")
        username = if (config.exists()) config.readText() else ""
        if (index.exists()) {
            files.addAll(index.readLines())
        }
        if (log.exists()) {
            val lines = log.readLines()
            if (lines.size % 3 != 0) {
                println("Bad log file")
            } else {
                for (i in 0 until lines.size / 3) {
                    val commitId = lines[3 * i]
                    val user = lines[3 * i + 1]
                    val message = lines[3 * i + 2]
                    commits.add(Commit(commitId, user, message))
                }
                System.err.println("${commits.size} commit found")
            }
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
                files.add(filename)
                index.toPath().writeLines(files)
                System.err.println("${file.absoluteFile} size: ${file.toPath().fileSize()}")
                println("The file '${filename}' is tracked.")
            } else {
                println("Can't find '$filename'.")
            }
        }
    }

    fun commit(args: Array<String>) {
        if (args.isEmpty()) {
            println("Message was not passed.")
            return
        }
        val message = args[0]
        val md = MessageDigest.getInstance("SHA-1")
        for (filename in files) {
            val file = File(filename)
            System.err.println(file.absolutePath)
            if (file.exists()) {
                md.update(file.readBytes())
            } else {
                System.err.println("$filename not found")
            }
        }
        val hash = md.digest().toHexString()
        System.err.println("hash: ${hash}")
        if (commits.isEmpty() || commits.last().commitId != hash) {
            commits.add(Commit(hash, username, message))
            log.appendText(hash + System.lineSeparator() +
                    username + System.lineSeparator() +
                    message + System.lineSeparator())
            val newCommitDir = File(commitsDir, hash)
            for (filename in files) {
                val file = File(filename)
                file.copyTo(File(newCommitDir, filename))
            }
            println("Changes are committed.")
        } else {
            println("Nothing to commit.")
        }
    }

    fun log(args: Array<String>) {
//        println("Show commit logs.")
        if (commits.isEmpty()) {
            println("No commits yet.")
        } else {
            for (commit in commits.reversed()) {
                println("commit ${commit.commitId}")
                println("Author: ${commit.author}")
                println(commit.message)
                println()
            }
        }
    }

    fun chechout(args: Array<String>) {
//        println("Restore a file.")
        if (args.isEmpty()) {
            println("Commit id was not passed.")
            return
        }
        val commitId = args[0]
        val commit = commits.find { it.commitId == commitId }
        if (commit == null) {
            println("Commit does not exist.")
        } else {
            val commitDir = File(commitsDir, commit.commitId).toPath()
            val files = commitDir.listDirectoryEntries()
            for (file in files) {
                val targetFile = File(file.toFile().name)
                file.copyTo(targetFile.toPath(), overwrite = true)
            }
            println("Switched to commit ${commit.commitId}.")
        }
    }
}