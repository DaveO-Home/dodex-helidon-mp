package golf.handicap.db

import golf.handicap.Golfer
import handicap.grpc.Command
import handicap.grpc.ListPublicGolfers
import jakarta.persistence.EntityManager
import java.sql.SQLException

interface IPopulateGolfer {
    @Throws(SQLException::class, InterruptedException::class)
    fun getGolfer(handicapGolfer: Golfer, cmd: Int, entityManager: EntityManager): Golfer

    @Throws(SQLException::class, InterruptedException::class)
    fun listGolfers(
        entityManager: EntityManager
    ): ListPublicGolfers.Builder

    @Throws(SQLException::class, InterruptedException::class)
    fun getGolfers(
        command: Command,
        entityManager: EntityManager
    ): String
}