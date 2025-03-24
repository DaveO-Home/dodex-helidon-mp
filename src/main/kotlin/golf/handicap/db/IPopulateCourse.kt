package golf.handicap.db

//import io.smallrye.mutiny.Uni
import handicap.grpc.HandicapData
import handicap.grpc.ListCoursesResponse
import jakarta.persistence.EntityManager
import java.sql.SQLException

interface IPopulateCourse {

    @Throws(SQLException::class, InterruptedException::class)
    fun getCourseWithTee(
        courseMap: java.util.HashMap<String, Any>,
        entityManager: EntityManager
    ): HandicapData?

    fun listCourses(
        state: String, entityManager: EntityManager
    ): ListCoursesResponse.Builder

    fun getCourses(
        state: String, entityManager: EntityManager
    ): String
}