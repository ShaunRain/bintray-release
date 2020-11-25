package com.novoda.gradle.release.internal

import com.novoda.gradle.release.MavenPublicationAttachments
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.component.SoftwareComponent
import org.gradle.api.tasks.javadoc.Javadoc

class AndroidAttachments extends MavenPublicationAttachments {
    private static final String ANDROID_SOFTWARE_COMPONENT_COMPAT_5_3 = 'com.novoda.gradle.release.AndroidSoftwareComponentCompat_Gradle_5_3'

    AndroidAttachments(String publicationName, Project project, def variant, boolean customAAR) {
        super(androidComponentFrom(project),
                androidSourcesJarTask(project, publicationName, variant),
                androidJavadocsJarTask(project, publicationName, variant),
                androidArchivePath(variant, customAAR))
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

    private static def androidArchivePath(def variant, boolean customAAR) {
        println("androidArchivePath: ${customAAR}")
        println("androidArchivePath: ${variant.outputs[0].packageLibrary}")
        return customAAR ? null : variant.outputs[0].packageLibrary
    }
}
