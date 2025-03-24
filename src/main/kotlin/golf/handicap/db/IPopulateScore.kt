package golf.handicap.db

import golf.handicap.Score
import jakarta.persistence.EntityManager

interface IPopulateScore {
    fun setScore(score: Score, entityManager: EntityManager): String
}