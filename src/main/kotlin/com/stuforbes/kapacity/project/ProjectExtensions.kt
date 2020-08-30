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