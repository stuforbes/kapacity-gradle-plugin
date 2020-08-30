/*
 * Copyright (c) 2020 com.stuforbes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.stuforbes.kapacity.project

import com.stuforbes.kapacity.KapacityPluginExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ProjectExtensionsTest {

    @MockK
    private lateinit var project: Project

    @MockK
    private lateinit var extensions: ExtensionContainer

    @BeforeEach
    fun before() {
        every { project.project } returns project
        every { project.extensions } returns extensions
    }

    @Nested
    inner class KapacityExtension {
        @Test
        fun `creates a new KapacityPluginExtension from the project`() {
            val pluginExtension = mockk<KapacityPluginExtension>()

            every { extensions.create("kapacity", KapacityPluginExtension::class.java) } returns pluginExtension

            assertThat(project.kapacityExtension()).isEqualTo(pluginExtension)
        }
    }

    @Nested
    inner class CompileClasspathFiles {
        @Test
        fun `looks up the compile class path files from the correct configuration`() {
            val configurations = mockk<ConfigurationContainer>()
            val configuration = mockk<Configuration>()
            val fileCollection = mockk<FileCollection>()

            every { project.configurations } returns configurations
            every { configurations.getByName("compileClasspath") } returns configuration
            every { configuration.fileCollection() } returns fileCollection

            assertThat(project.compileClasspathFiles()).isEqualTo(fileCollection)
        }
    }

    @Nested
    inner class FindBuildDirectoriesForSourceSets {

        @MockK
        private lateinit var sourceSetContainer: SourceSetContainer

        @MockK
        private lateinit var mainSourceSet: SourceSet

        @MockK
        private lateinit var otherSourceSet: SourceSet

        @BeforeEach
        fun before() {
            every { extensions.getByType(SourceSetContainer::class.java) } returns sourceSetContainer
            every { sourceSetContainer.iterator() } returns listOf(mainSourceSet, otherSourceSet).toMutableList().iterator()

            every { mainSourceSet.name } returns MAIN_SOURCE_SET_NAME
            every { otherSourceSet.name } returns OTHER_SOURCE_SET_NAME
        }

        @Test
        fun `returns the runtime classpath of the source set, if it was found`() {
            val runtimeClasspath = mockk<FileCollection>()

            every { mainSourceSet.runtimeClasspath } returns runtimeClasspath

            assertThat(project.findBuildDirectoriesForSourceSets(MAIN_SOURCE_SET_NAME)).isEqualTo(runtimeClasspath)
        }

        @Test
        fun `returns all project files if the source set could not be found`() {
            val projectFiles = mockk<ConfigurableFileCollection>()

            every { project.files() } returns projectFiles

            assertThat(project.findBuildDirectoriesForSourceSets("different")).isEqualTo(projectFiles)
        }
    }

    private companion object {
        const val MAIN_SOURCE_SET_NAME = "main"
        const val OTHER_SOURCE_SET_NAME = "other"
    }
}