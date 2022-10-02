package io.github.pantheooon

import org.apache.commons.dbcp.BasicDataSource
import org.junit.Assert
import org.junit.Test
import io.github.pantheooon.engine.support.DefaultMissionEngine
import io.github.pantheooon.repository.support.JackSonSerialize
import io.github.pantheooon.repository.support.MemoryMissionRepository
import io.github.pantheooon.repository.support.MySqlMissionRepository
import java.util.*


class EngineTest {

    @Test
    fun test_mission_engine() {

        val dbRepository = MemoryMissionRepository()
        val missionEngine = DefaultMissionEngine(dbRepository, JackSonSerialize())
        val result = missionEngine.executeMission(FailedMission())
        Assert.assertEquals(result.success, false)
    }


    @Test
    fun test_serialize() {
        val failedMission = FailedMission()
        val encode = JackSonSerialize().encode(failedMission)
        Assert.assertEquals("""{"name":"Pantheon"}""".trimIndent(), encode)
    }

    fun test_interval() {
        val dbRepository = MemoryMissionRepository()
        val missionEngine = DefaultMissionEngine(dbRepository, JackSonSerialize())
        missionEngine.executeMission(FailedMission())
        val now = Date()




        Thread.sleep(5_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()



        Thread.sleep(5_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()




        Thread.sleep(5_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()


        Thread.sleep(5_000)
        println(dbRepository.compensateRecords())

    }


    @Test
    fun test_exponent() {
        val dbRepository = MemoryMissionRepository()
        val missionEngine = DefaultMissionEngine(dbRepository, JackSonSerialize())
        missionEngine.executeMission(ExponentMission())
        val now = Date()




        Thread.sleep(1_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()



        Thread.sleep(3_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()




        Thread.sleep(4_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()


        Thread.sleep(5_000)
        println(dbRepository.compensateRecords())

    }


    @Test
    fun test_rollback_mission() {
        val dbRepository = MemoryMissionRepository()
        val missionEngine = DefaultMissionEngine(dbRepository, JackSonSerialize())
        missionEngine.executeMission(RollBackMissionImpl())
        val now = Date()


        Thread.sleep(1_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()



        Thread.sleep(2_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()




        Thread.sleep(4_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()


        println("-------------->")
        Thread.sleep(1_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()

        Thread.sleep(2_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()

        Thread.sleep(4_000)
        println("$now : ${dbRepository.compensateRecords()[0].nextExecuteDate}")
        missionEngine.compensate()


        println(dbRepository.compensateRecords())

    }


    @Test
    fun test_update_success() {
        val dbRepository = MemoryMissionRepository()
        val missionEngine = DefaultMissionEngine(dbRepository, JackSonSerialize())
        missionEngine.executeMission(SuccessMission())
        Assert.assertEquals(dbRepository.compensateRecords().size, 0)
    }


    @Test
    fun test_task_store_mysql() {

        val dataSource = BasicDataSource().also {
            it.driverClassName = "com.mysql.cj.jdbc.Driver"
            it.url = "jdbc:mysql://localhost:3307/pmj_test"
            it.username = "root"
            it.password = "123456"
        }
        val dbRepository = MySqlMissionRepository(dataSource)
        val missionEngine = DefaultMissionEngine(dbRepository, JackSonSerialize())
        val mission = RollBackMissionImpl()
//        missionEngine.executeMission(mission)
        missionEngine.compensate()
    }

    @Test
    fun test_complex_properties() {
        val dataSource = BasicDataSource().also {
            it.driverClassName = "com.mysql.cj.jdbc.Driver"
            it.url = "jdbc:mysql://localhost:3307/pmj_test"
            it.username = "root"
            it.password = "123456"
        }
        val dbRepository = MySqlMissionRepository(dataSource)
        val missionEngine = DefaultMissionEngine(dbRepository, JackSonSerialize())

        var mission = ComplexPropertyMissionImpl().also {
            it.id = "name"
            it.list = mutableListOf(
                Person(
                    name = "name",
                    ages = mutableListOf(1, 2, 3)
                ), Person(
                    name = "pantheon",
                    ages = mutableListOf(1, 2, 3, 4)
                )
            )
            it.map = mutableMapOf(
                "19" to mutableListOf(
                    Person(
                        name = "19",
                        ages = mutableListOf(1, 2, 3)
                    ), Person(
                        name = "pantheon",
                        ages = mutableListOf(1, 2, 3, 4)
                    )
                ), "20" to mutableListOf(
                    Person(
                        name = "20",
                        ages = mutableListOf(1, 2, 3)
                    ), Person(
                        name = "pantheon",
                        ages = mutableListOf(1, 2, 3, 4)
                    )
                )
            )
            it.date = Date()
        }

        missionEngine.executeMission(mission)

        Thread.sleep(1000)
        missionEngine.compensate()

    }
}