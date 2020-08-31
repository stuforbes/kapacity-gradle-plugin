/*
 * Copyright (c) 2020 com.stuforbes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("UnstableApiUsage")

package com.stuforbes.kapacity

import com.stuforbes.kapacity.project.compileClasspathFiles
import com.stuforbes.kapacity.project.findBuildDirectoriesForSourceSets
import com.stuforbes.kapacity.project.kapacityExtension
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskContainer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

typealias ActionTask = Action<Task>
typealias ActionProject = Action<Project>
typealias ActionJavaExec = Action<JavaExec>

@ExtendWith(MockKExtension::class)
internal class KapacityPluginTest {

    @MockK
    private lateinit var project: Project

    @MockK
    private lateinit var task: Task

    @RelaxedMockK
    private lateinit var exec: JavaExec

    @MockK
    private lateinit var extension: KapacityPluginExtension

    @MockK
    private lateinit var tasks: TaskContainer

    private lateinit var taskConfigurerSlot: CapturingSlot<ActionTask>
    private lateinit var afterEvaluateSlot: CapturingSlot<ActionProject>
    private lateinit var execConfigurerSlot: CapturingSlot<ActionJavaExec>

    private lateinit var plugin: KapacityPlugin

    @BeforeEach
    fun before() {
        mockkStatic("com.stuforbes.kapacity.project.ProjectExtensionsKt")

        plugin = KapacityPlugin()

        primeSlots()

        every { project.kapacityExtension() } returns extension
        every { project.tasks } returns tasks
    }

    @Test
    fun `should create a new kapacity task on the project`() {
        plugin.apply(project)

        verify { project.task(KAPACITY_TASK_NAME, any<ActionTask>()) }
    }

    @Test
    fun `invoking the task's configure action adds an afterEvaluate action to the project`() {
        primeFilesOnProject()

        plugin.apply(project)
        taskConfigurerSlot.captured.execute(task)

        verify { project.afterEvaluate(any<ActionProject>()) }
    }

    @Nested
    inner class RunKapacityTest {

        private lateinit var allCompileSources: FileCollection

        @BeforeEach
        fun before() {

            allCompileSources = primeFilesOnProject()

            every { tasks.getByName(KAPACITY_TASK_NAME) } returns task
            every { task.dependsOn(any()) } returns task
        }

        @Test
        fun `running the after evaluate action should create a new exec task`() {
            runAfterEvaluate()
            verify { tasks.create(KAPACITYEXEC_TASK_NAME, JavaExec::class.java, any()) }
        }

        @Test
        fun `should configure the java exec correctly`() {
            every { extension.applyTo(exec) } just Runs

            runAfterEvaluate()

            execConfigurerSlot.captured.execute(exec)

            verify {
                exec.group = JavaBasePlugin.VERIFICATION_GROUP
                exec.description = "Runs Kapacity tests"
                exec.main = "com.stuforbes.kapacity.console.ConsoleRunnerKt"
                exec.classpath = allCompileSources

                extension.applyTo(exec)
            }
        }

        @Test
        fun `should setup the kapacity task to depend on the exec task`() {
            runAfterEvaluate()

            verify { task.dependsOn(exec) }
        }

        private fun runAfterEvaluate() {
            plugin.apply(project)
            taskConfigurerSlot.captured.execute(task)
            afterEvaluateSlot.captured.execute(project)
        }
    }

    private fun primeFilesOnProject(): FileCollection {
        val compileClasspathFiles = mockk<FileCollection>()
        val sourceSetBuildDirectories = mockk<FileCollection>()
        val allCompileSources = mockk<FileCollection>()

        every { project.compileClasspathFiles() } returns compileClasspathFiles
        every { project.findBuildDirectoriesForSourceSets("main") } returns sourceSetBuildDirectories
        every { sourceSetBuildDirectories.plus(compileClasspathFiles) } returns allCompileSources

        return allCompileSources
    }

    private fun primeSlots() {
        taskConfigurerSlot = slot()
        every { project.task(any(), capture(taskConfigurerSlot)) } returns task

        afterEvaluateSlot = slot()
        every { project.afterEvaluate(capture(afterEvaluateSlot)) } just Runs

        execConfigurerSlot = slot()
        every { tasks.create(KAPACITYEXEC_TASK_NAME, JavaExec::class.java, capture(execConfigurerSlot)) } returns exec
    }

    private companion object {
        const val KAPACITY_TASK_NAME = "kapacity"
        const val KAPACITYEXEC_TASK_NAME = "kapacityexec"
    }
}