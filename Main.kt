package flashcards

import java.io.File
import kotlin.random.Random

val random = Random
val flashcards = mutableListOf<Flashcard>()
var log = ""

fun main(args: Array<String>) {
    if (args.contains("-import")) importCards(args[args.indexOf("-import") + 1])

    while (true) {
        println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")

        when (readLine()!!.addToLog()) {
            "exit" -> {
                println ("Bye bye!")
                break
            }
            "add" -> add()
            "ask" -> ask()
            "remove" -> remove()
            "import" -> importCards()
            "export" -> exportCards()
            "log" -> saveLog()
            "hardest card" -> hardestCard()
            "reset stats" -> resetStats()
        }
    }

    if (args.contains("-export")) exportCards(args[args.indexOf("-export") + 1])
}

data class Flashcard(var term: String, var def: String) {
    var fails = 0

    override fun toString(): String = "$term\n$def\n$fails"
}

fun input(cardOrDef: String): Pair<String, Boolean> {
    val input = readLine()!!.addToLog()
    val contains = if (cardOrDef == "card") flashcards.containsTerm(input) else flashcards.containsDef(input)

    return if (contains) Pair("The $cardOrDef \"$input\" already exists.\n", false) else return Pair(input, true)
}

fun add() {
    println("The card:")

    val card = input("card")

    if (!card.second) return println(card.first)

    println("The definition of the card:")

    val def = input("definition")

    if (!def.second) return println(def.first) else {
        flashcards.add(Flashcard(card.first, def.first))

        println("The pair (\"${card.first}\":\"${def.first}\") has been added.\n")
    }
}

fun remove() {
    println("Which card?")

    val card = readLine()!!.addToLog()

    if (flashcards.removeIf { it.term == card }) {
        println("The card has been removed.\n")
    } else println("Can't remove \"$card\": there is no such card.\n")
}

fun ask() {
    println("How many times to ask?")

    repeat(readLine()!!.addToLog().toInt()) {
        val card = flashcards[random.nextInt(flashcards.size)]

        println("Print the definition of \"${card.term}\":")

        val answer = readLine()!!.addToLog()

        if (card.def == answer) println("Correct!") else {
            card.fails ++

            val text = if (flashcards.containsDef(answer)) {
                ", but your definition is correct for \"${flashcards.first { it.def == answer }.term}\""
            } else ""

            println("Wrong. The right answer is \"${card.def}\"$text.")
        }
    }

    println()
}

fun importCards(arg: String = "") {
    if (arg.isEmpty()) println("File name:")

    val fileName = if (arg.isEmpty()) readLine()!!.addToLog() else arg

    if (File(fileName).exists()) {
        val cards = File(fileName).readLines()

        for (i in cards.indices step 3) {
            if (flashcards.containsTerm(cards[i])) flashcards.removeIf { it.term == cards[i] }
            if (flashcards.containsDef(cards[i + 1])) flashcards.removeIf { it.def == cards[i + 1] }

            val card = Flashcard(cards[i], cards[i + 1])

            card.fails = cards[i + 2].toInt()

            flashcards.add(card)
        }

        println("${cards.size / 3} cards have been loaded.\n")
    } else println("File not found.\n")
}

fun exportCards(arg: String = "") {
    if (arg.isEmpty()) println("File name:")

    val fileName = if (arg.isEmpty()) readLine()!!.addToLog() else arg

    val cards = File(fileName).writeText(flashcards.joinToString("\n") { it.toString() })

    println("${flashcards.size} cards have been saved.\n")
}

fun saveLog() {
    println("File name:")

    val fileName = readLine()!!.addToLog()

    println("The log has been saved.\n")

    val log = File(fileName).writeText(log)
}

fun hardestCard() {
    val hardestCards = flashcards

    hardestCards.sortByDescending { it.fails }
    hardestCards.removeIf { it.fails != hardestCards[0].fails || it.fails == 0}

    if (hardestCards.isEmpty()) println("There are no cards with errors.\n") else {
        var (text1, text3) = arrayOf(" is ", "it")
        val text2 = if (hardestCards[0].fails > 1) "s" else ""

        if (hardestCards.size > 1) {
            text1 = "s are "
            text3 = "them"
        }

        println("The hardest card$text1${hardestCards.termsToString()}. " +
                "You have ${hardestCards[0].fails} error$text2 answering $text3.\n")
    }
}

fun resetStats() {
    flashcards.forEach { it.fails = 0 }

    println("Card statistics have been reset.")
}

fun MutableList<Flashcard>.containsTerm(term: String): Boolean {
    this.forEach { if (it.term == term) return true }

    return false
}

fun MutableList<Flashcard>.containsDef(def: String): Boolean {
    this.forEach { if (it.def == def) return true }

    return false
}

fun MutableList<Flashcard>.termsToString(): String {
    val terms = mutableListOf<String>()

    this.forEach { terms.add(it.term) }

    return terms.joinToString("\", \"", "\"", "\"")
}

fun println(message: Any?) {
    System.out.println(message)

    log += "$message\n"
}

fun String.addToLog(): String {
    log += "$this\n"

    return this
}