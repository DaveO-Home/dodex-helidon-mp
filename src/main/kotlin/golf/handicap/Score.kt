package golf.handicap

import java.io.Serializable

class Score
    : Serializable {
    var netScore = 0.0.toFloat()
    var handicap = 0.0.toFloat()
    var grossScore = 0
    var adjustedScore = 0
    var teeTime: String? = null
    var message: String? = null
    var tees: String? = null
    var course: Course? = null
    var golfer: Golfer? = null
    var status = 0
    private var scoreId = 0
    fun init() {
        adjustedScore = 0
        grossScore = 0
        handicap = 0.0.toFloat()
        netScore = handicap
        teeTime = null
        tees = null
        message = null
        course = null
        golfer = null
        status = 0
        scoreId = 0
    }

    override fun toString(): String {
        return "AdjustedScore, GrossScore, Handicap, NetScore, TeeTime, Tees, Message, Course, Golfer, Status, ScoreId " +
                "('$adjustedScore, $grossScore, $handicap, $netScore, $teeTime, $tees, $message, $course, $golfer, $status, $scoreId')"
    }
}