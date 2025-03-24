package golf.handicap.db

import golf.handicap.*
import golf.handicap.entities.PersistedRatings
import golf.handicap.entities.PersistedScores
import jakarta.json.Json
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import jakarta.persistence.Query
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaDelete
import jakarta.persistence.criteria.Root
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.*
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.Year
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Date
import java.util.logging.Logger


class PopulateGolferScores : SqlConstants(), IPopulateGolferScores {
    private val teeDate = SimpleDateFormat("yyyy-MM-dd")
    private val teeYear = SimpleDateFormat("yyyy")
    private var beginDate: String? = null
    private var endDate: String? = null
    private var maxRows = 20
    private var gettingData = false
    private var beginGolfDate: Date? = null
    private var endGolfDate: Date? = null
    private var overlapYears = false

    companion object {
        private val LOGGER = Logger.getLogger(PopulateGolferScores::class.java.name)
    }

    private fun setPeriod(year: String) {
        beginDate = "$year-01-01"
        endDate = (year.toInt() + 1).toString() + "-01-01"
        beginGolfDate = beginDate?.let { teeDate.parse(it, ParsePosition(0)) }
        endGolfDate = endDate?.let { teeDate.parse(it, ParsePosition(0)) }
    }

    fun useCurrentYearOnly(thisYearOnly: Boolean) {
        overlapYears = thisYearOnly
    }

    override fun getGolferScores(golfer: Golfer, rows: Int, entityManager: EntityManager): Map<String, Any?> {
        val em = entityManager.entityManagerFactory.createEntityManager()
        val oldRows = this.maxRows
        this.maxRows = rows
        gettingData = true

        val mapData = getGolferScores(golfer, em)

        gettingData = false
        this.maxRows = oldRows

        return mapData
    }

    @Throws(Exception::class)
    fun getGolferScores(golfer: Golfer, em: EntityManager): Map<String, Any?> {
        val tableMap: MutableMap<String, Any> = HashMap()

        val golferPin = golfer.pin
        val previousYear = Year.now().value - 1
        var isCalcHandicap = false
        overlapYears = golfer.overlap
        beginDate = if (overlapYears) "01-01-$previousYear" else beginDate
        val query: Query?

        query =
            if (gettingData) {
                if (golferPin.isNullOrEmpty()) {
                    em.createQuery(
                        GOLFER_SCORES.replace(
                            ":R", "golfer.firstName = :first and golfer.lastName = :last and " +
                                    "golfer.publicDisplay and "
                        ),
                        PersistedScores::class.java
                    )
                        .setMaxResults(maxRows)
                        .setParameter("first", golfer.firstName)
                        .setParameter("last", golfer.lastName)
                } else {
                    em.createQuery(
                        GOLFER_SCORES.replace(":R", "golfer.pin = :pin and "), PersistedScores::class.java
                    )
                        .setMaxResults(maxRows)
                        .setParameter("pin", golfer.pin)
                }
            }
            /* Used to calculate golfer's handicap */
            else {
                isCalcHandicap = true

                em.createQuery(
                    HANDICAP_DATA, PersistedScores::class.java
                ).setMaxResults(maxRows)
                    .setParameter("pin", golfer.pin)
            }

        val scoreObjects = query
            .setParameter("begin", dateToLocalDateTime(beginGolfDate!!))
            .setParameter("end", dateToLocalDateTime(endGolfDate!!))
            .resultList

        val tableArray = Json.createArrayBuilder()
        scoreObjects.forEach { score ->
            val tableObject = Json.createObjectBuilder()
            var persistedRating = PersistedRatings()

            if (isCalcHandicap) {
                val ratingSet: Set<PersistedRatings> = score.course?.ratings!!

                for (rating in ratingSet) {
                    if (score.courseTees == rating.tee) {
                        persistedRating = rating
                    }
                }
            }

            if (isCalcHandicap) {
                tableObject.add("PIN", score.pin)
                    .add("TEE_RATING", persistedRating.teeRating.toString())
                    .add("TEE_SLOPE", persistedRating.teeSlope.toString())
                    .add("ADJUSTED_SCORE", score.adjustedScore.toString())
                    .add("TEE_TIME", score.teeTime.toString())
                    .add("COURSE_SEQ", persistedRating.course_seq.toString())
                    .add("TEE_PAR", persistedRating.teePar.toString())
            } else {
                tableObject.add("COURSE_NAME", score.course?.courseName)
                    .add("GROSS_SCORE", score.grossScore.toString())
                    .add("NET_SCORE", BigDecimal(score.netScore.toString()).setScale(1, RoundingMode.UP))
                    .add("ADJUSTED_SCORE", score.adjustedScore.toString())
                    .add("HANDICAP", BigDecimal(score.handicap.toString()).setScale(1, RoundingMode.UP))
                    .add("COURSE_TEES", score.courseTees.toString())
                    .add(
                        "TEE_TIME",
                        if (gettingData) score.teeTime.toString().substring(0, 10) else score.teeTime.toString()
                    )
                    .add("USED", if (score.used == null) "" else score.used.toString())
            }
            tableArray.add(tableObject.build())
        }

        tableMap["array"] = tableArray.build()

        return tableMap
    }

    override fun removeLastScore(golferPIN: String?, entityManager: EntityManager): String {
        val em = entityManager.entityManagerFactory.createEntityManager()
        val count: Int
        var used: String
        val query = em.createQuery(LAST_SCORE, LocalDateTime::class.java)
        query.setParameter("pin", golferPIN)

        val localDateTime: LocalDateTime?
        val transaction = em.transaction

        try {
            localDateTime = query.singleResult
            transaction.begin()

            count = deleteUsingTeeTime(golferPIN!!, localDateTime, em)

            em.flush()
            transaction.commit()
            used = count.toString()
        } catch (e: NoResultException) {
            used = "-1"
        } catch (ex: Exception) {
            used = "-1"
            transaction.rollback()
        } finally {
            em.close()
        }

        return used
    }

    private fun deleteUsingTeeTime(golferPIN: String, localDateTime: LocalDateTime, em: EntityManager): Int {
        try {
            val cb: CriteriaBuilder = em.criteriaBuilder
            val cd: CriteriaDelete<PersistedScores> = cb.createCriteriaDelete(PersistedScores::class.java)
            val scores: Root<PersistedScores> = cd.from(PersistedScores::class.java)
            cd.where(
                cb.equal(scores.get<String>("pin"), golferPIN),
                cb.and(cb.equal(scores.get<LocalDateTime>("teeTime"), localDateTime))
            )
            return em.createQuery(cd).executeUpdate()
        } finally {
            //
        }
    }

    @Throws(SQLException::class, InterruptedException::class)
    override fun setGolferHandicap(golfer: Golfer, em: EntityManager): Int {

        var rowsUpdated: Int = em.createQuery(UPDATE_HANDICAP)
            .setParameter("handicap", golfer.handicap)
            .setParameter("pin", golfer.pin).executeUpdate()

        val score = golfer.score ?: return rowsUpdated

        rowsUpdated += em.createQuery(UPDATE_SCORES)
            .setParameter("handicap", golfer.handicap)
            .setParameter("net", 0.0f)
            .setParameter("seq", score.course?.course_key)
            .setParameter("pin", golfer.pin)
            .setParameter("time", score.teeTime).executeUpdate()

        em.flush()
        return rowsUpdated
    }

    @Throws(Exception::class)
    fun setUsed(pin: String?, course: Int, teeTime: String, em: EntityManager): Int {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val teeDate = LocalDateTime.parse("$teeTime:00".replace("T", " "), formatter)

        val count: Int = em.createQuery(SET_USED)
            .setParameter("used", '*')
            .setParameter("seq", course)
            .setParameter("time", teeDate)
            .setParameter("pin", pin).executeUpdate()

        return count
    }

    @Throws(Exception::class)
    fun clearUsed(pin: String?, em: EntityManager): Int {

        val count: Int = em.createQuery(RESET_SCORES)
            .setParameter("used", Character.valueOf(' '))
            .setParameter("pin", pin).executeUpdate()
        em.flush()

        return count
    }

    init {
        setPeriod(teeYear.format(Date()))
    }

    private fun dateToLocalDateTime(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }
}
