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
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetContainer

private const val KAPACITY_EXTENSION_NAME = "kapacity"
private const val COMPILE_CLASSPATH_CONFIGURATION = "compileClasspath"

// TODO - can we remove the extra getProject() call here?
fun Project.kapacityExtension() = project
        .extensions
        .create(KAPACITY_EXTENSION_NAME, KapacityPluginExtension::class.java)

// TODO - can we remove the extra getProject() call here?
fun Project.compileClasspathFiles() = project
        .configurations
        .getByName(COMPILE_CLASSPATH_CONFIGURATION)
        .fileCollection()

fun Project.findBuildDirectoriesForSourceSets(sourceSet: String): FileCollection {
    // TODO - can we remove the extra getProject() call here?
    val sourceSetContainer = project.extensions.getByType(SourceSetContainer::class.java)
    val sourceSet = sourceSetContainer.find { ssc -> ssc.name == sourceSet }
    return sourceSet
            ?.runtimeClasspath
            ?: this.files()
}