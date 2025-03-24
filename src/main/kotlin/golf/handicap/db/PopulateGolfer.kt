@file:JvmName("PopulateGolfer")

package golf.handicap.db

import golf.handicap.Golfer
import golf.handicap.Score
import golf.handicap.entities.PersistedGolfer
import handicap.grpc.Command
import handicap.grpc.Golfer.newBuilder
import handicap.grpc.ListPublicGolfers
import jakarta.json.Json
import jakarta.json.JsonObject

import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.transaction.Transactional
import org.apache.logging.log4j.LogManager

import java.sql.SQLException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PopulateGolfer : SqlConstants(), IPopulateGolfer {
    companion object {
        private val LOGGER = LogManager.getLogger(PopulateGolfer::class.java.name)
    }

    @Transactional
    @Throws(SQLException::class, InterruptedException::class)
    override fun getGolfer(handicapGolfer: Golfer, cmd: Int, entityManager: EntityManager): Golfer {
        val em = entityManager.entityManagerFactory.createEntityManager()
        val builder = entityManager.criteriaBuilder
        val golferCriteria = builder.createQuery(PersistedGolfer::class.java)
        val root = golferCriteria.from(PersistedGolfer::class.java)
        var returnGolfer = Golfer()
        var resultGolfer: PersistedGolfer? = null
        var isGolferFound = false

        handicapGolfer.message = "Golfer not found"
        if (handicapGolfer.pin!!.trim { it <= ' ' } != "") {
            val query: CriteriaQuery<PersistedGolfer> = golferCriteria.select(root)
                .where(builder.equal(root.get<PersistedGolfer.GolferId>("pin"), handicapGolfer.pin))
            val typedQuery: TypedQuery<PersistedGolfer> = em.createQuery(query)

            try {
                resultGolfer = typedQuery.singleResult
                returnGolfer = buildReturnGolfer(resultGolfer)
                isGolferFound = true
            } catch (nre: NoResultException) {
                try {
                    val equalFirstName = builder.equal(root.get<String>("firstName"), handicapGolfer.firstName)
                    val equalLastName = builder.equal(root.get<String>("lastName"), handicapGolfer.lastName)
                    val queryByName: CriteriaQuery<PersistedGolfer> =
                        golferCriteria.select(root) //.where(equalFirstName, equalLastName)

                    val criteria = queryByName.select(root).where(builder.and(equalFirstName, equalLastName))
                    val golferTypedQuery: TypedQuery<PersistedGolfer> = em.createQuery(criteria)
                    resultGolfer = golferTypedQuery.singleResult
                    returnGolfer = buildReturnGolfer(resultGolfer)
                    isGolferFound = true
                } catch (nre: NoResultException) {
                    handicapGolfer.status = -1
                    if (handicapGolfer.firstName!!.length < 3 || handicapGolfer.lastName!!.length < 5) {
                        handicapGolfer.status = -1
                        var which = "Last"
                        if (handicapGolfer.firstName!!.length < 3) {
                            which = "First"
                        }
                        handicapGolfer.message = "$which name required for new golfer."
                        return handicapGolfer
                    }
                }
            } catch (e: Exception) {
                handicapGolfer.status = -1
                handicapGolfer.message = e.message
                return handicapGolfer
            }

            val persistedGolfer: PersistedGolfer = transformGolfer(handicapGolfer, resultGolfer, cmd)

            em.transaction.begin()
            try {
                if (isGolferFound && cmd == 8) {
                    val golferQuery = em.createQuery(UPDATE_GOLFER_HANDICAP)
                        .setParameter(1, handicapGolfer.overlap)
                        .setParameter(2, handicapGolfer.public)
                        .setParameter(3, handicapGolfer.country)
                        .setParameter(4, handicapGolfer.state)
                        .setParameter(5, handicapGolfer.handicap)
                        .setParameter(6, LocalDateTime.now().withNano(0))
                        .setParameter(7, handicapGolfer.pin)

                    golferQuery.executeUpdate()
                } else if (!isGolferFound) {
                    em.persist(persistedGolfer)
                }
                em.flush()
                em.transaction.commit()

                returnGolfer = buildReturnGolfer(persistedGolfer)
                returnGolfer.message = "Logged In"
                returnGolfer.status = 0
            } catch (e: Exception) {
                if (resultGolfer?.lastName != null) {
                    handicapGolfer.status = -3
                } else {
                    handicapGolfer.status = -2
                }
                handicapGolfer.message = e.message
                em.transaction.rollback()
                e.printStackTrace()
            } finally {
                em.close()
            }
        }

        return returnGolfer
    }

    @Throws(SQLException::class, InterruptedException::class)
    override fun listGolfers(entityManager: EntityManager):
            ListPublicGolfers.Builder {
        val em = entityManager.entityManagerFactory.createEntityManager()
        val builder = entityManager.criteriaBuilder
        val golferCriteria = builder.createQuery(PersistedGolfer::class.java)
        val root = golferCriteria.from(PersistedGolfer::class.java)

        val query: CriteriaQuery<PersistedGolfer> = golferCriteria.select(root)
        val golfers: List<PersistedGolfer> = em.createQuery(query).resultList

        val golfersBuilder = ListPublicGolfers.newBuilder()
        var golferBuilder: handicap.grpc.Golfer.Builder?

        golfers.forEach { golfer ->
            val concatName = golfer.firstName + ", " + golfer.lastName
            golferBuilder = newBuilder() // handicap.grpc.Golfer.newBuilder()

            golferBuilder!!.name = concatName
            golfersBuilder.addGolfer(golferBuilder)
        }

        return golfersBuilder
    }

    @Throws(SQLException::class, InterruptedException::class)
    override fun getGolfers(command: Command, entityManager: EntityManager):
            String {
        val em = entityManager.entityManagerFactory.createEntityManager()
        val builder = entityManager.criteriaBuilder
        val golferCriteria = builder.createQuery(PersistedGolfer::class.java)
        val root = golferCriteria.from(PersistedGolfer::class.java)

        val query: CriteriaQuery<PersistedGolfer> = golferCriteria.select(root)
        val golfers: List<PersistedGolfer> = em.createQuery(query).resultList

        val golfersBuilder = ListPublicGolfers.newBuilder()
        var golferBuilder: handicap.grpc.Golfer.Builder?

        golfers.forEach { golfer ->
            val concatName = golfer.firstName + " " + golfer.lastName
            golferBuilder = newBuilder() // handicap.grpc.Golfer.newBuilder()

            golferBuilder!!.name = concatName
            golfersBuilder.addGolfer(golferBuilder)
        }
        val golfersJson = convertBuilderToJson(golfersBuilder)

        return golfersJson
    }

    /*
        Helidon MP at this time does not support 'repeated' message
        The javascript client was changed to handle the 'HandicapData' object
     */
    private fun convertBuilderToJson(golfersBuilder: ListPublicGolfers.Builder): String {
        val responseObject = Json.createObjectBuilder()
        val responseArray = Json.createArrayBuilder()

        golfersBuilder.golferList.forEach { golfer ->
            responseArray.add(golfer.name)
        }
        val jsonObject = responseObject.add("golfer", responseArray).build()

        return jsonObject.toString()
    }

    private fun buildReturnGolfer(resultGolfer: PersistedGolfer): Golfer {
        val handicapGolfer = Golfer()

        handicapGolfer.pin = resultGolfer.pin
        resultGolfer.golferId = handicapGolfer.pin?.let { PersistedGolfer.GolferId(it) }
        handicapGolfer.firstName = resultGolfer.firstName
        handicapGolfer.lastName = resultGolfer.lastName
        handicapGolfer.handicap = resultGolfer.handicap?.toDouble() ?: 0.0 //.toFloat()
        handicapGolfer.country = resultGolfer.country
        handicapGolfer.state = resultGolfer.state
        handicapGolfer.overlap = resultGolfer.overlapYears == true
        handicapGolfer.public = resultGolfer.publicDisplay == true

        val localDateTime = LocalDateTime.now()
        val zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
        val date = zdt.toInstant().toEpochMilli()
        handicapGolfer.lastLogin = date

        return handicapGolfer
    }

    private fun buildHandicapGolfer(golferObject: JsonObject): Golfer {
        val golfer = Golfer()
        golfer.pin = golferObject.getString("pin")
        golfer.firstName = golferObject.getString("firstName")
        golfer.lastName = golferObject.getString("lastName")
        golfer.status = golferObject.getInt("status")
        golfer.handicap = golferObject.getJsonNumber("handicap").doubleValue()
        golfer.country = golferObject.getString("country")
        golfer.message = if (golferObject.isNull("message")) "" else golferObject.getString("message")
        golfer.course = golferObject.getString("course")
        golfer.overlap = golferObject.getBoolean("overlap")
        golfer.public = golferObject.getBoolean("public")
        val score = Score()
        golfer.score = score // golferObject.getString("score")
        golfer.tee = golferObject.getInt("tee")
        golfer.teeDate = golferObject.getJsonNumber("teeDate").longValue()
        return golfer
    }

    private fun transformGolfer(handicapGolfer: Golfer, resultGolfer: PersistedGolfer?, cmd: Int): PersistedGolfer {
        val persistedGolfer = PersistedGolfer()

        val isLogin = cmd == 3 || handicapGolfer.pin?.length == 0
        persistedGolfer.pin = resultGolfer?.pin ?: handicapGolfer.pin
        persistedGolfer.golferId = handicapGolfer.pin?.let { PersistedGolfer.GolferId(it) }
        persistedGolfer.firstName = resultGolfer?.firstName ?: handicapGolfer.firstName
        persistedGolfer.lastName = resultGolfer?.lastName ?: handicapGolfer.lastName
        persistedGolfer.handicap = resultGolfer?.handicap ?: handicapGolfer.handicap.toFloat()
        persistedGolfer.lastLogin = LocalDateTime.now().withNano(0)
        /* Login first before changing values with subsequent login
        *  cmd = 3 - first login
        *  cmd = 8 - subsequent login button clicks
        * */
        persistedGolfer.country =
            if (cmd == 8 || resultGolfer?.country == null) handicapGolfer.country else resultGolfer.country
        persistedGolfer.state =
            if (cmd == 8 || resultGolfer?.state == null) handicapGolfer.state else resultGolfer.state
        persistedGolfer.overlapYears =
            if (cmd == 8 || resultGolfer?.overlapYears == null) handicapGolfer.overlap else resultGolfer.overlapYears
        persistedGolfer.publicDisplay =
            if (cmd == 8 || resultGolfer?.publicDisplay == null) handicapGolfer.public else resultGolfer.publicDisplay

        return persistedGolfer
    }

    private fun getGolferInsert(newGolfer: PersistedGolfer): String {
        val sb = StringBuilder()
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
        val dateTime =
            newGolfer.lastLogin?.toLocalDate()?.format(dateFormatter) + " " + newGolfer.lastLogin?.toLocalTime()
                ?.format(timeFormatter)

        sb.append("insert into PersistedGolfer (country,firstName,handicap,lastLogin,lastName,overlapYears,publicDisplay,state,pin) values (")
        sb.append("'").append(newGolfer.country).append("','").append(newGolfer.firstName).append("',")
        sb.append(newGolfer.handicap).append(",").append("?1").append(",'")
            .append(newGolfer.lastName).append("',")
            .append(newGolfer.overlapYears).append(",").append(newGolfer.publicDisplay).append(",'")
            .append(newGolfer.state).append("','").append(newGolfer.pin).append("')")

        return sb.toString()
    }
}
