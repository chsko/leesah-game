package no.nav.quizmaster.questions

import no.nav.quizrapid.Answer
import no.nav.quizrapid.Question
import java.time.Duration

class Deduplication(
    interval: Duration,
    maxCount: Int,
    active: Boolean
) : QuestionCategory("deduplication", maxCount, active, interval = interval) {

    private var question: Question = Question(category = category, question = "answer this question only once with an <you wont dupe me!>. hjelp til oppgaven: https://navikt.github.io/leesah-game/oppgaver/deduplication")

    private val questions = mutableMapOf<String, TeamSheet>()
    private val resetAnswer = "you duped me!"
    private val fasit = "you wont dupe me!"

    data class TeamSheet(val completed: MutableSet<String> = mutableSetOf(), val teamAnswers: MutableMap<String, List<String>> = mutableMapOf())


    override fun check(answer: Answer) {
        val sheet = questions[answer.questionId]
        if (sheet == null) {
            logger.warn("answer: ${answer.json()} does not match a question")
            return
        }

        if(answer.answer == resetAnswer) {
            sheet.teamAnswers[answer.teamName] = emptyList()
            sheet.completed.remove(answer.teamName)
            logger.info("team = ${answer.teamName} for question: ${answer.questionId} reset in deduplication")
            return
        }
        if(answer.answer != fasit) {
            false.publish(answer.teamName, answer.questionId, answer.messageId)
            sheet.completed.remove(answer.teamName)
        }
        sheet.teamAnswers.compute(answer.teamName) { key, value, -> if(value == null)  listOf(answer.messageId) else value + answer.messageId }

        if (sheet.teamAnswers[answer.teamName]!!.size > 1) {
            false.publish(answer.teamName, answer.questionId, answer.messageId)
            sheet.completed.remove(answer.teamName)
            logger.info("team = ${answer.teamName} received failing assessment due to multiple answers")
        }
        publishAssessments(answer.questionId, sheet)
    }

    override fun newQuestions(): List<Question> {
        if(questions[question.messageId] == null) {
            questions[question.messageId] = TeamSheet()
        }
        return listOf(question)
    }

    override fun sync(question: Question): Boolean {
        questions[question.id()] = TeamSheet()
        return true
    }

    private fun publishAssessments(questionId: String, sheet: TeamSheet) {
        sheet.teamAnswers.filter { it.value.size == 1 && it.key !in sheet.completed }.forEach {
            true.publish(it.key, questionId, it.value[0])
            sheet.completed.add(it.key)
        }
    }
}