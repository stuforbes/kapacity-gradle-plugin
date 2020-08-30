/*
 * Copyright (c) 2020 com.stuforbes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.stuforbes.kapacity

import org.gradle.api.tasks.JavaExec

class MissingConfigurationException(name: String) : RuntimeException("No definition for value '$name'")

open class KapacityPluginExtension {
    var duration: Long? = null
    var dataLoader: String? = null
    var timeSeriesDataSorter: String? = null
    var dataPoster: String? = null
    var flightRecorder: String? = null
    var resultPrinter: String? = null
    var resultFormatter: String? = null

    fun applyTo(exec: JavaExec) {
        val args = listOf(
            arg(FLAG_DURATION, duration.toString(), FIELD_NAME_DURATION),
            arg(FLAG_DATA_LOADER, dataLoader, FIELD_NAME_DATA_LOADER),
            arg(FLAG_DATA_SORTER, timeSeriesDataSorter, FIELD_NAME_DATA_SORTER),
            arg(FLAG_DATA_POSTER, dataPoster, FIELD_NAME_DATA_POSTER),
            arg(FLAG_FLIGHT_RECORDER, flightRecorder, FIELD_NAME_FLIGHT_RECORDER)
        ).flatten()
            .filterNotNull()
            .toMutableList()
            .addOptionalArg(FLAG_RESULT_PRINTER, resultPrinter)
            .addOptionalArg(FLAG_RESULT_FORMATTER, resultFormatter)

        exec.args(args.toMutableList())
    }

    private fun <T> T?.orError(name: String) = this ?: throw MissingConfigurationException(name)

    private fun <T> arg(flag: String, value: T?, fieldName: String) = listOf("-$flag", value.orError(fieldName))

    private fun <T> MutableList<Any>.addOptionalArg(flag: String, value: T?): MutableList<Any>{
        if(value != null) {
            add("-$flag")
            add(value)
        }
        return this
    }

    companion object {
        private const val FLAG_DURATION = "t"
        private const val FLAG_DATA_LOADER = "dl"
        private const val FLAG_DATA_SORTER = "ds"
        private const val FLAG_DATA_POSTER = "dp"
        private const val FLAG_FLIGHT_RECORDER = "fr"
        private const val FLAG_RESULT_PRINTER = "rp"
        private const val FLAG_RESULT_FORMATTER = "rf"

        private const val FIELD_NAME_DURATION = "duration"
        private const val FIELD_NAME_DATA_LOADER = "dataLoader"
        private const val FIELD_NAME_DATA_SORTER = "dataSorter"
        private const val FIELD_NAME_DATA_POSTER = "dataPoster"
        private const val FIELD_NAME_FLIGHT_RECORDER = "flightRecorder"

    }
}