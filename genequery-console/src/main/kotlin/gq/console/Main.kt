package gq.console
import gq.console.commands.BASE_COMMAND_NAME
import gq.console.commands.BulkQueryCommand
import gq.console.commands.runCommand
import org.apache.commons.cli.*;
import java.io.PrintWriter

val NAME_TO_COMMAND = listOf(
        BulkQueryCommand()
).associate { it.name() to it }

fun printIntroductionHelp() {
    val formatter = HelpFormatter()
    val options = Options()
    options.addOption("h", "help", false, "Print this help message")
    formatter.printHelp("$BASE_COMMAND_NAME <COMMAND> [arguments]", options)

    val options2 = Options()
    formatter.optPrefix = ""
    NAME_TO_COMMAND.forEach { options2.addOption(it.key, false, it.value.description()) }

    val pw = PrintWriter(System.out)
    formatter.printOptions(pw, formatter.width, options2, formatter.leftPadding, formatter.descPadding)
    pw.flush()
}

fun main(args: Array<String>) {
    if (args.isEmpty() || args.first() == "-h" || args.first() == "--help") {
        printIntroductionHelp()
        return
    }
    if (args.first() !in NAME_TO_COMMAND) {
        println("Unknown command: ${args.first()}")
        printIntroductionHelp()
        return
    }
    runCommand(NAME_TO_COMMAND[args.first()]!!, args.sliceArray(1..args.size-1))
}