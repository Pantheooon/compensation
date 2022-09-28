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
package press.pantheon.model

import java.util.*

data class MissionRecord(
    val missionCode:String,
    val className: String,
    var properties:String,
    var errorMsg:String?,
    var status: Int,
    var times: Int,
    var nextExecuteDate: Date,
    var created: Date,
    var updated: Date

) {
    var id: String? = UUID.randomUUID().toString()
}

class MissionRecordStatus {
    companion object {
        const val waitingToStart = 0
        const val processing = 1
        const val success = 2
        const val failed = 3
    }
}

