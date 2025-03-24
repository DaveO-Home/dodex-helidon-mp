package golf.handicap.db

import golf.handicap.Golfer
import jakarta.persistence.EntityManager

interface IPopulateGolferScores {
    @Throws(Exception::class)
    fun getGolferScores(golfer: Golfer, rows: Int, entityManager: EntityManager): Map<String, Any?>
    @Throws(Exception::class)
    fun removeLastScore(golferPIN: String?, entityManager: EntityManager): String
    @Throws(Exception::class)
    fun setGolferHandicap(golfer: Golfer, em: EntityManager): Int
}