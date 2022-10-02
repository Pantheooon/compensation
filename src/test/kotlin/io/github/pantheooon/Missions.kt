package io.github.pantheooon

import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.pantheooon.mission.*
import java.util.Date
import java.util.concurrent.TimeUnit


@MissionMeta(
    missionCode = "failed_mission",
    retryTimes = 3,
    retryType = RetryType.INTERVAL,
    gapTimeUnit = TimeUnit.SECONDS,
    gapTime = 5
)
class FailedMission : Mission {


    val name: String = "Pantheon"

    @JsonIgnore
    val type: Int = 20

    override fun execute(): MissionExecutedResult {
        return MissionExecutedResult(success = false, errorMsg = "failed")
    }

}


@MissionMeta(
    missionCode = "failed_mission",
    retryTimes = 3,
    retryType = RetryType.EXPONENT,
    gapTimeUnit = TimeUnit.SECONDS,
    gapTime = 1
)
class ExponentMission : Mission {


    override fun execute(): MissionExecutedResult {
        return MissionExecutedResult(success = false, errorMsg = "failed")
    }

}



@MissionMeta(
    missionCode = "failed_mission",
    retryTimes = 3,
    retryType = RetryType.EXPONENT,
    gapTimeUnit = TimeUnit.SECONDS,
    gapTime = 1
)
class RollBackMissionImpl : RollbackAbleMission {

    var id:String? = null

    override fun execute(): MissionExecutedResult {

        println("execute--->$id")
        return MissionExecutedResult(success = false, errorMsg = "failed")
    }

    override fun rollback(): MissionExecutedResult {
        println("rollback---$id")
        return MissionExecutedResult(success = false, errorMsg = "failed")
    }

}

@MissionMeta(
    missionCode = "success_mission",
    retryTimes = 3,
    retryType = RetryType.EXPONENT,
    gapTimeUnit = TimeUnit.SECONDS,
    gapTime = 1
)
class SuccessMission : Mission {
    override fun execute(): MissionExecutedResult {
        return MissionExecutedResult(success = true)
    }

}

data class Person(
    var name:String,
    var ages:List<Int>
)

@MissionMeta(
    missionCode = "failed_mission",
    retryTimes = 3,
    retryType = RetryType.EXPONENT,
    gapTimeUnit = TimeUnit.SECONDS,
    gapTime = 1
)
class ComplexPropertyMissionImpl : RollbackAbleMission {

    var id:String? = null

    var list:List<Person>? = null

    var map:Map<String,List<Person>>? = null

    var date:Date? = null


    override fun execute(): MissionExecutedResult {

        println("execute--->$id,$list,$map,$date")
        return MissionExecutedResult(success = false, errorMsg = "failed")
    }

    override fun rollback(): MissionExecutedResult {
        println("rollback---$id")
        return MissionExecutedResult(success = true, errorMsg = "failed")
    }

}