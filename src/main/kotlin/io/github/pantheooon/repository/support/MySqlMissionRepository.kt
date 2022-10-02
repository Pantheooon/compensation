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
package io.github.pantheooon.repository.support

import io.github.pantheooon.model.MissionRecord
import io.github.pantheooon.repository.MissionRepository
import java.sql.Timestamp
import java.util.*
import javax.sql.DataSource

class MySqlMissionRepository(val dataSource: DataSource) : MissionRepository {


    override fun saveMission(record: MissionRecord) {
        val connection = dataSource.connection
        val statement =
            connection.prepareStatement("insert into mission_record(id,mission_code,class_name,properties,error_msg,status,times,next_execute_date,created,updated) values (?,?,?,?,?,?,?,?,?,?)")
        try {
            with(statement) {
                setString(1, record.id)
                setString(2, record.missionCode)
                setString(3, record.className)
                setString(4, record.properties)
                setString(5, record.errorMsg)
                setInt(6, record.status)
                setInt(7, record.times)
                setTimestamp(8, Timestamp(record.nextExecuteDate.time))
                setTimestamp(9, Timestamp(record.created.time))
                setTimestamp(10, Timestamp(record.updated.time))
                executeUpdate()
            }

        } finally {
            connection?.close()
            statement?.close()
        }
    }

    override fun updateMission(record: MissionRecord) {
        val connection = dataSource.connection
        val statement =
            connection.prepareStatement("update  mission_record set mission_code = ?,class_name = ? ,properties = ?,error_msg = ?,status = ?,times = ?,next_execute_date = ?,updated = ? where id = ?")
        try {
            with(statement) {
                setString(1, record.missionCode)
                setString(2, record.className)
                setString(3, record.properties)
                setString(4, record.errorMsg)
                setInt(5, record.status)
                setInt(6, record.times)
                setTimestamp(7, Timestamp(record.nextExecuteDate.time))
                setTimestamp(8, Timestamp(record.updated.time))
                setString(9, record.id)
                executeUpdate()
            }
        } finally {
            connection?.close()
            statement?.close()
        }

    }

    override fun cleanUp(expired: Date) {

        val connection = dataSource.connection
        val statement =
            connection.prepareStatement("delete from mission_record where status = 2 and updated < ?")
        try {
            statement.setTimestamp(1, Timestamp(expired.time))
            statement.executeUpdate()
        } finally {
            connection?.close()
            statement?.close()
        }


    }

    override fun compensateRecords(): List<MissionRecord> {
        val res = mutableListOf<MissionRecord>()
        val connection = dataSource.connection
        val statement =
            connection.prepareStatement("select * from mission_record where status = 0 and next_execute_date <= ?")
        statement.setTimestamp(1, Timestamp(System.currentTimeMillis()))
        val resultSet = statement.executeQuery()

        while (resultSet.next()) {
            res.add(
                MissionRecord(
                    missionCode = resultSet.getString("mission_code"),
                    className = resultSet.getString("class_name"),
                    properties = resultSet.getString("properties"),
                    errorMsg = resultSet.getString("error_msg"),
                    status = resultSet.getInt("status"),
                    times = resultSet.getInt("times"),
                    nextExecuteDate = Date(resultSet.getTimestamp("next_execute_date").time),
                    created = Date(resultSet.getTimestamp("created").time),
                    updated = Date(resultSet.getTimestamp("updated").time),
                ).also { it.id = resultSet.getString("id") })
        }

        return res
    }
}