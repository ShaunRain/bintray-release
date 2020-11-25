package com.novoda.gradle.release.internal

import com.novoda.gradle.release.MavenPublicationAttachments
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.util.GradleVersion

class AndroidAttachments extends MavenPublicationAttachments {
    private static final String ANDROID_SOFTWARE_COMPONENT_COMPAT_5_3 = 'com.novoda.gradle.release.AndroidSoftwareComponentCompat_Gradle_5_3'

    AndroidAttachments(String publicationName, Project project, def variant) {
        super(androidComponentFrom(project),
                androidSourcesJarTask(project, publicationName, variant),
                androidJavadocsJarTask(project, publicationName, variant))
    }

    private static SoftwareComponent androidComponentFrom(Project project) {
        def clazz = this.classLoader.loadClass(ANDROID_SOFTWARE_COMPONENT_COMPAT_5_3)
        return project.objects.newInstance(clazz) as SoftwareComponent
    }

    private static Task androidSourcesJarTask(Project project, String publicationName, def variant) {
        def sourcePaths = variant.sourceSets.collect { it.javaDirectories }.flatten()
        return sourcesJarTask(project, publicationName, sourcePaths)
    }

    private static Task androidJavadocsJarTask(Project project, String publicationName, def variant) {
        Javadoc javadoc = project.task("javadoc${publicationName.capitalize()}", type: Javadoc) { Javadoc javadoc ->
            javadoc.source = variant.javaCompiler.source
            javadoc.classpath = variant.javaCompiler.classpath
        } as Javadoc
        return javadocsJarTask(project, publicationName, javadoc)
    }

    private static def androidArchivePath(def variant) {
        println("androidArchivePath: ${variant.outputs[0].packageLibrary}")
        return variant.outputs[0].packageLibrary
    }
}
