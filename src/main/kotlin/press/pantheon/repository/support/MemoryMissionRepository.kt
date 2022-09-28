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
package press.pantheon.repository.support

import press.pantheon.model.MissionRecord
import press.pantheon.model.MissionRecordStatus.Companion.waitingToStart
import press.pantheon.repository.MissionRepository
import java.util.*


class MemoryMissionRepository : MissionRepository {

    private val missionList = mutableListOf<MissionRecord>()


    override fun saveMission(mission: MissionRecord):Unit  = missionList.run {
        add(mission)
    }


    override fun updateMission(mission: MissionRecord): Unit = missionList.run {
        removeIf { it.id == mission.id }
        add(mission)
    }

    override fun cleanUp(expired: Date): Unit = missionList.run {
        removeIf { it.created < expired }
    }

    override fun compensateRecords(): List<MissionRecord> = missionList.filter { it.status == waitingToStart && it.nextExecuteDate <= Date()}
}