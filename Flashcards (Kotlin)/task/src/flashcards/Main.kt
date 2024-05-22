import java.io.File
import kotlin.random.Random

data class FlashCard(val term: String, val definition: String, var mistakes: Int)

var logText = ""

fun main(args: Array<String>) {
    val flashCards = mutableSetOf<FlashCard>()
    var importPathArg: String? = null
    var exportPathArg: String? = null

    for (argument in args.indices) {
        when (args[argument]) {
            "-import" -> importPathArg = args.getOrElse(argument + 1) { null }
            "-export" -> exportPathArg = args.getOrElse(argument + 1) { null }
        }
    }

    if (importPathArg != null) {
        importFlashCards(importPathArg).let {
            if (it.isNotEmpty()) {
                flashCards += it
                println("${it.size} cards have been loaded.")
            } else {
                println("File not found.")
            }
            println()
        }
    }

    while (true) {
        println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")

        when (readln().lowercase()) {
            "add" -> {
                println("The Card:")
                val term = readln()
                if (isTermInFlashCards(flashCards, term)) {
                    println("The card \"${term}\" already exists.")
                } else {
                    println("The definition of the card:")
                    val definition = readln()
                    if (isDefinitionInFlashCards(flashCards, definition)) {
                        println("The definition \"${definition}\" already exists.")
                    } else {
                        flashCards.add(FlashCard(term = term, definition = definition, mistakes = 0))
                        println("The pair (\"$term\":\"$definition\") has been added.")
                    }
                }
            }
            "remove" -> {
                println("Which card?")
                val term = readln()
                if (isTermInFlashCards(flashCards, term)) {
                    flashCards.removeIf { it.term == term }
                    println("The card has been removed.")
                } else {
                    println("Can't remove \"${term}\": there is no such card.")
                }
            }
            "import" -> {
                println("File name:")
                importFlashCards(readln()).let {
                    if (it.isNotEmpty()) {
                        flashCards += it
                        println("${it.size} cards have been loaded.")
                    } else {
                        println("File not found.")
                    }
                }
            }
            "export" -> {
                println("File name:")
                exportFlashCards(flashCards, readln())
                println("${flashCards.size} cards have been saved.")
            }
            "ask" -> {
                println("How many times to ask?")
                val times = readln().toInt()
                repeat(times) {
                    val randomIndex = Random.nextInt(0, flashCards.size)
                    println("Print the definition of \"${flashCards.elementAt(randomIndex).term}\":")
                    val definition = readln()
                    val correctFlashCard = flashCards.elementAt(randomIndex)
                    if (correctFlashCard.definition == definition) {
                        println("Correct!")
                    } else {
                        val guessedFlashCard = flashCards.find { it.definition == definition }
                        if (guessedFlashCard != null) {
                            correctFlashCard.mistakes += 1
                            println("Wrong. The right answer is \"${correctFlashCard.definition}\", but your definition is correct for \"${guessedFlashCard.term}\".")
                        } else {
                            correctFlashCard.mistakes += 1
                            println("Wrong. The right answer is \"${correctFlashCard.definition}\".")
                        }
                    }
                }
            }
            "exit" -> {
                if (exportPathArg != null) {
                    exportFlashCards(flashCards, exportPathArg)
                    println("Bye bye!")
                    println("${flashCards.size} cards have been saved.")
                } else {
                    println("Bye bye!")
                }
                break
            }
            "log" -> {
                println("File name:")
                val fileName = readln()
                File(fileName).writeText(logText)
                println("The log has been saved.")
            }
            "hardest card" -> {
                val flashCardsWithMostMistakes = findFlashCardWithMostMistakes(flashCards)
                if (flashCardsWithMostMistakes.isEmpty()) {
                    println("There are no cards with errors.")
                } else if (flashCardsWithMostMistakes.size == 1) {
                    println("The hardest card is \"${flashCardsWithMostMistakes[0].term}\". You have ${flashCardsWithMostMistakes[0].mistakes} errors answering it.")
                } else {
                    val flashCardsTerms = flashCardsWithMostMistakes.joinToString(separator = ", ") { "\"${it.term}\"" }
                    println("The hardest cards are $flashCardsTerms. You have ${flashCardsWithMostMistakes[0].mistakes} errors answering them")
                }
            }
            "reset stats" -> {
                flashCards.forEach { card ->
                    card.mistakes = 0
                }
                println("Card statistics have been reset.")
            }
            else -> {
                println()
                continue
            }
        }
        println()
    }
}

fun isTermInFlashCards(flashCards: MutableSet<FlashCard>, term: String): Boolean {
    return flashCards.any { it.term == term }
}

fun isDefinitionInFlashCards(flashCards: MutableSet<FlashCard>, definition: String): Boolean {
    return flashCards.any { it.definition == definition }
}

fun findFlashCardWithMostMistakes(flashCards: MutableSet<FlashCard>): List<FlashCard> {
    val maxMistakes = flashCards.maxByOrNull { it.mistakes }?.mistakes ?: return emptyList()
    return if (maxMistakes == 0) emptyList() else flashCards.filter { it.mistakes == maxMistakes }
}

fun println(str: String = "") {
    kotlin.io.println(str)
    logText += str + "\n"
}

fun readln(): String {
    val str = kotlin.io.readln()
    logText += str + "\n"
    return str
}

fun importFlashCards(fileName: String): MutableSet<FlashCard> {
    val file = File(fileName)
    val flashCards = mutableSetOf<FlashCard>()
    if (file.exists()) {
        file.readLines().forEach {
            val (term, definition, mistakes) = it.split(":")
            flashCards += FlashCard(term = term, definition = definition, mistakes = mistakes.toInt())
        }
        return flashCards
    }
    return mutableSetOf()
}

fun exportFlashCards(flashCards: MutableSet<FlashCard>, fileName: String) {
    File(fileName).writeText(flashCards.joinToString("\n") {
        "${it.term}:${it.definition}:${it.mistakes}"
    })
}