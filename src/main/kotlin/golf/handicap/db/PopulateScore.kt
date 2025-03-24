package golf.handicap.db

import golf.handicap.Score
import golf.handicap.entities.PersistedScores
import jakarta.json.bind.JsonbBuilder
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import jakarta.persistence.Query
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.transaction.Transactional
import java.time.LocalDateTime
import java.util.logging.Logger

class PopulateScore : SqlConstants(), IPopulateScore {
    companion object {
        private val LOGGER = Logger.getLogger(PopulateScore::class.java.name)
    }

    @Transactional
    override fun setScore(
        score: Score,
        entityManager: EntityManager
    ): String {
        val em = entityManager.entityManagerFactory.createEntityManager()

        val scores = getScoreByTeetime(score, em)

        if (scores.isEmpty()) {
            val persistedScores = buildScore(score)
            val transaction = em.transaction
            try {
                transaction.begin()
                em.persist(persistedScores)
                em.flush()
                transaction.commit()
            } catch (e: Exception) {
                transaction.rollback()
                e.printStackTrace()
                score.status = -1
                score.message = "Persist Score Failed"
            } finally {
                em.close()
            }
        } else { //if(scores.size == 1) {
            updateScore(score, em)
        }
        val jsonData = JsonbBuilder.create().toJson(score)

        return jsonData
    }

    private fun getScoreByTeetime(score: Score, em: EntityManager): MutableSet<Score> {
        val scores = mutableSetOf<Score>()
        val builder = em.criteriaBuilder
        val scoreCriteria: CriteriaQuery<PersistedScores> = builder.createQuery(PersistedScores::class.java)
        val root = scoreCriteria.from(PersistedScores::class.java)

        val pinPredicate = builder.equal(root.get<PersistedScores.ScoresId>("pin"), score.golfer!!.pin)
        val teeTimePredicate = builder.equal(
            root.get<PersistedScores>("teeTime"),
            score.teeTime?.let { LocalDateTime.parse(it) })
        val coursePredicate = builder.equal(root.get<PersistedScores.ScoresId>("courseSeq"), score.course!!.course_key)
//        val timePredicate = builder.equal(root.get<PersistedScores.ScoresId>("teeTime"), score.teeTime)

        scoreCriteria.where(
            pinPredicate,
            builder.and(teeTimePredicate),
            builder.and(coursePredicate),
            builder.and(teeTimePredicate)
        )
        try {
            val queryScores = em.createQuery(scoreCriteria).resultList
            for (queryScore in queryScores) {
                val newScore = Score()
                newScore.golfer = golf.handicap.Golfer()
                newScore.course = score.course
                newScore.golfer!!.pin = queryScore.scoresId!!.pin
                newScore.grossScore = queryScore.grossScore!!.toInt()
                newScore.netScore = queryScore.netScore!!
                newScore.adjustedScore = queryScore.adjustedScore!!
                newScore.teeTime = queryScore.teeTime.toString()
                newScore.handicap = queryScore.handicap!!
                newScore.golfer!!.handicap = queryScore.handicap!!.toDouble()
                newScore.course!!.course_key = queryScore.course!!.courseSeq
                newScore.course!!.teeId = queryScore.courseTees!!
                newScore.tees = queryScore.courseTees!!.toString()
                scores.add(newScore)
            }
        } catch (nre: NoResultException) {
            LOGGER.info(nre.message)
        } catch (e: Exception) {
            LOGGER.severe(e.message)
        }

        return scores
    }

    private fun buildScore(score: Score): PersistedScores {
        val persistedScores = PersistedScores()

        persistedScores.grossScore = score.grossScore
        persistedScores.netScore = score.netScore
        persistedScores.adjustedScore = score.adjustedScore
        persistedScores.pin = score.golfer!!.pin
        persistedScores.courseSeq = score.course!!.course_key
        persistedScores.courseTees = score.course!!.teeId
        persistedScores.handicap = score.handicap
        persistedScores.teeTime = score.teeTime?.let { LocalDateTime.parse(it) }
        val scoreId =
            PersistedScores.ScoresId(score.golfer!!.pin!!, score.course!!.course_key, persistedScores.teeTime!!)
        persistedScores.scoresId = scoreId
        persistedScores.used = persistedScores.used ?: ' '
        return persistedScores
    }

    @Transactional
    private fun updateScore(score: Score, em: EntityManager): Score {
        val transaction = em.transaction

        try {
            transaction.begin()

            val query = scoreUpdateQuery(score, em)
            query.executeUpdate()
            em.flush()

            updateGolfer(score, em)
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            e.printStackTrace()
            score.status = -1
            score.message = "Update Score/Golfer Failed"
        } finally {
            em.close()
        }

        return score
    }

    private fun scoreUpdateQuery(score: Score, em: EntityManager): Query {
        val teeTime = score.teeTime?.let { LocalDateTime.parse(it) }

        return em.createQuery(UPDATE_SCORE)
            .setParameter("net", score.netScore)
            .setParameter("gross", score.grossScore)
            .setParameter("adjusted", score.adjustedScore)
            .setParameter("handicap", score.handicap)
            .setParameter("course", score.course?.course_key)
            .setParameter("pin", score.golfer?.pin)
            .setParameter("tee", score.course?.teeId)
            .setParameter("time", teeTime)
    }

    @Transactional
    private fun updateGolfer(score: Score, em: EntityManager): Score {
        try {
            val query =
                em.createQuery(UPDATE_GOLFER)
                    .setParameter(1, score.golfer!!.overlap)
                    .setParameter(2, score.golfer!!.public)
                    .setParameter(3, score.golfer!!.pin)
            query.executeUpdate()
            em.flush()
        } catch (e: Exception) {
            score.status = -1
            score.message = "Update Golfer failed"
            e.printStackTrace()
            throw Exception("Update Golfer Failed: " + e.cause)
        }

        return score
    }
}
