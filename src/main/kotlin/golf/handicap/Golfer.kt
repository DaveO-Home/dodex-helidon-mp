package golf.handicap

import java.util.HashMap

class Golfer : Cloneable {
    companion object {}

    var firstName: String? = null
    var lastName: String? = null
    var pin: String? = null
    var country: String? = null
    var state: String? = null
    var handicap = 0.0
    var score: Score? = null
    var overlap: Boolean = true
    var public: Boolean = false
    var status: Int = 0
    var lastLogin: Long? = null
    var course: String? = null
    var tee: Int = 0
    var teeDate: Long? = null
    var message: String? = null

    public override fun clone(): Any {
        return super.clone()
    }

    override fun toString(): String {
        return "Golfer=$firstName $lastName Pin=$pin Handicap=$handicap Country=$country State=$state Overlap=$overlap Public=$public"
    }

    fun getMap(): Map<String, Any?> {
        val groupMap: MutableMap<String, Any?> = HashMap()
        groupMap["firstName"] = this.firstName
        groupMap["lastName"] = this.lastName
        groupMap["pin"] = this.pin
        groupMap["country"] = this.country
        groupMap["state"] = this.state
        groupMap["handicap"] = handicap
        groupMap["score"] = this.score
        groupMap["overlap"] = this.overlap
        groupMap["public"] = this.public
        groupMap["status"] = this.status
        groupMap["lastLogin"] = this.lastLogin
        groupMap["course"] = this.course
        groupMap["tee"] = this.tee
        groupMap["teeDate"] = this.teeDate
        groupMap["message"] = this.message
        return groupMap
    }
}
