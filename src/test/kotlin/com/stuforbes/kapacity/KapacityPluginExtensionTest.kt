package com.stuforbes.kapacity

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.tasks.JavaExec
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class KapacityPluginExtensionTest {

    @RelaxedMockK
    private lateinit var exec: JavaExec

    private lateinit var extension: KapacityPluginExtension

    @BeforeEach
    fun before() {
        this.extension = KapacityPluginExtension().apply {
            duration = DURATION
            dataLoader = DATA_LOADER
            timeSeriesDataSorter = DATA_SORTER
            dataPoster = DATA_POSTER
            flightRecorder = FLIGHT_RECORDER
        }
    }

    @Test
    fun `sets the args on the exec correctly`() {
        extension.applyTo(exec)

        verify {
            exec.args(mutableListOf(
                    "-t", DURATION.toString(),
                    "-dl", DATA_LOADER,
                    "-ds", DATA_SORTER,
                    "-dp", DATA_POSTER,
                    "-fr", FLIGHT_RECORDER
            ))
        }
    }

    @Test
    fun `adds optional arguments if they're present`() {
        extension.resultFormatter = RESULT_FORMATTER
        extension.resultPrinter = RESULT_PRINTER

        extension.applyTo(exec)

        verify {
            exec.args(mutableListOf(
                    "-t", DURATION.toString(),
                    "-dl", DATA_LOADER,
                    "-ds", DATA_SORTER,
                    "-dp", DATA_POSTER,
                    "-fr", FLIGHT_RECORDER,
                    "-rp", RESULT_PRINTER,
                    "-rf", RESULT_FORMATTER
            ))
        }
    }

    @Test
    fun `reports an error if a mandatory value is missing`() {
        extension.dataPoster = null

        val ex = assertThrows<MissingConfigurationException> {
            extension.applyTo(exec)
        }
        assertThat(ex.message).isEqualTo("No definition for value 'dataPoster'")
    }

    private companion object {
        const val DURATION: Long = 1234L
        const val DATA_LOADER: String = "DATA_LOADER"
        const val DATA_SORTER: String = "DATA_SORTER"
        const val DATA_POSTER: String = "DATA_POSTER"
        const val FLIGHT_RECORDER: String = "FLIGHT_RECORDER"
        const val RESULT_FORMATTER: String = "RESULT_FORMATTER"
        const val RESULT_PRINTER: String = "RESULT_PRINTER"
    }
}