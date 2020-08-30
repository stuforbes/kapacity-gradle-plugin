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

import com.stuforbes.kapacity.project.compileClasspathFiles
import com.stuforbes.kapacity.project.findBuildDirectoriesForSourceSets
import com.stuforbes.kapacity.project.kapacityExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.JavaExec

class KapacityPlugin : Plugin<Project> {

    @Suppress("UnstableApiUsage")
    override fun apply(project: Project) {
        val extension = project.kapacityExtension()
        project.task(KAPACITY_TASK_NAME) {
            val compileClasspathFiles = project.compileClasspathFiles()

            val sourceSetBuildDirectories = project.findBuildDirectoriesForSourceSets(MAIN_SOURCE_SET)

            val allCompileSources = sourceSetBuildDirectories.plus(compileClasspathFiles)

            project.afterEvaluate {
                runKapacityTest(project, extension, allCompileSources)
            }
        }
    }

    private fun runKapacityTest(project: Project, extension: KapacityPluginExtension, cp: FileCollection) {
        val kapacityExecTask = project.tasks.create(KAPACITY_EXEC_TASK_NAME, JavaExec::class.java) {
            it.apply {
                group = EXEC_GROUP
                description = EXEC_DESCRIPTION
                main = EXEC_MAIN_CLASS_NAME
                classpath = cp
            }
            extension.applyTo(it)
        }

        val kapacityTask = project.tasks.getByName(KAPACITY_TASK_NAME)
        kapacityTask.dependsOn(kapacityExecTask)
    }

    private companion object {
        const val KAPACITY_TASK_NAME = "kapacity"
        const val KAPACITY_EXEC_TASK_NAME = "kapacityexec"
        const val MAIN_SOURCE_SET = "main"

        const val EXEC_GROUP = JavaBasePlugin.VERIFICATION_GROUP
        const val EXEC_DESCRIPTION = "Runs Kapacity tests"
        const val EXEC_MAIN_CLASS_NAME = "com.stuforbes.kapacity.console.ConsoleRunnerKt"
    }
}