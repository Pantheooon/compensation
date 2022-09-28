/**
 * MIT License
 * Copyright (c) 2022 Pantheon
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package press.pantheon.engine.support

import org.slf4j.LoggerFactory
import press.pantheon.engine.MissionEngine
import press.pantheon.mission.*
import press.pantheon.model.MissionRecord
import press.pantheon.model.MissionRecordStatus
import press.pantheon.repository.MissionRepository
import press.pantheon.repository.Serialize
import java.lang.Exception
import java.util.*

class DefaultMissionEngine(private val repository: MissionRepository, private val serialize: Serialize) :
    MissionEngine {

    val logger = LoggerFactory.getLogger(DefaultMissionEngine::class.java)

    override fun executeMission(mission: Mission): MissionExecutedResult {
        val meta = mission::class.java.getAnnotation(MissionMeta::class.java)
            ?: return MissionExecutedResult(success = false, errorMsg = "please annotated mission with MissionMeta")
        logger.info("mission start,missionCode:{}",meta.missionCode)
        var result: MissionExecutedResult = try {
            mission.execute()
        } catch (ex: Exception) {
            MissionExecutedResult(false, ex.message)
        }.also {
            if (!it.success) {
                val name = mission::class.java.name
                val now = Date()
                val record = MissionRecord(
                    missionCode = meta.missionCode,
                    className = name,
                    properties = serialize.encode(mission),
                    errorMsg = it.errorMsg,
                    status = MissionRecordStatus.waitingToStart,
                    times = 0,
                    nextExecuteDate = Date(now.time + meta.gapTimeUnit.toMillis(meta.gapTime)),
                    created = now,
                    updated = now
                )
                repository.saveMission(record)
            }
        }

        return result

    }

    override fun compensate() {

        val records = repository.compensateRecords()
        if (records.isEmpty()) {
            return
        }
        for (it in records) {
            with(it) {
                status = MissionRecordStatus.processing
                updated = Date()
                repository.updateMission(it)
            }

            val clazz = Class.forName(it.className)
            val mission = serialize.decode(it.properties, clazz as Class<Mission>)
            val meta = clazz.getAnnotation(MissionMeta::class.java)

            if (mission == null) {
                with(it) {
                    status = MissionRecordStatus.failed
                    errorMsg = "can not deserialize to Mission object,please check ${it.className} if it exists"
                    updated = Date()
                    repository.updateMission(it)
                }
                return
            }
            val executedResult: MissionExecutedResult = if (mission is RollbackAbleMission) {
                if (it.times >= meta.retryTimes) {
                    (mission as RollbackAbleMission).rollback()
                } else {
                    mission.execute()
                }

            } else {
                mission.execute()
            }

            updateRecords(executedResult, mission, it)
        }


    }

    private fun updateRecords(executedResult: MissionExecutedResult, mission: Mission, record: MissionRecord) {
        if (executedResult.success) {
            with(record) {
                status = MissionRecordStatus.success
                updated = Date()
                repository.updateMission(this)
            }
        } else {
            val meta = mission::class.java.getAnnotation(MissionMeta::class.java)
            val currentTimes = record.times + 1
            val moveToNextStatus: (Int) -> Unit = {
                if (currentTimes >= it) {
                    with(record) {
                        status = MissionRecordStatus.failed
                        errorMsg = executedResult.errorMsg
                        times = currentTimes
                        updated = Date()
                        repository.updateMission(this)
                    }
                } else {
                    var nextExecution = Date(record.created.time + meta.gapTimeUnit.toMillis(meta.gapTime) * (currentTimes + 1))
                    if (meta.retryType == RetryType.EXPONENT) {
                        nextExecution =
                            Date(
                                record.nextExecuteDate.time + 1.shl(if (currentTimes >= meta.retryTimes) currentTimes - meta.retryTimes else currentTimes)
                                        * meta.gapTimeUnit.toMillis(meta.gapTime)
                            )
                    }
                    with(record) {
                        properties = serialize.encode(mission)
                        status = MissionRecordStatus.waitingToStart
                        times = currentTimes
                        nextExecuteDate = nextExecution
                        errorMsg = executedResult.errorMsg
                        updated = Date()
                        repository.updateMission(this)
                    }
                }
            }
            if (mission is RollbackAbleMission && currentTimes >= meta.retryTimes) {
                moveToNextStatus(2 * meta.retryTimes)
            } else {
                moveToNextStatus(meta.retryTimes)
            }

        }
    }

    override fun cleanUp(expired: Date) = repository.cleanUp(expired)


}