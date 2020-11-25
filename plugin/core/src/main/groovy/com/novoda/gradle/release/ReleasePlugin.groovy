package com.novoda.gradle.release

import com.jfrog.bintray.gradle.BintrayPlugin
import com.novoda.gradle.release.internal.AndroidAttachments
import com.novoda.gradle.release.internal.JavaAttachments
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

class ReleasePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        PublishExtension extension = project.extensions.create('publish', PublishExtension)
        project.afterEvaluate {
            extension.validate()
            attachArtifacts(extension, project)
            new BintrayConfiguration(extension).configure(project)
        }
        project.apply([plugin: 'maven-publish'])

        def bintrayPlugin = new BintrayPlugin() {
            @Override
            void apply(Project p) {
                super.apply(project)
                PublishExtension ext = p.extensions.findByName('publish')
                p.getTasks().findByName('bintrayUpload').doLast {
                    if (!state.failure) {
                        println "可添加依赖使用：\n\n\timplementation '${ext.groupId}:${ext.artifactId}:${ext.publishVersion}'\n\n"
                    }
                }
            }
        }

        bintrayPlugin.apply(project)

//        if (!extension.builderName.isEmpty()) {
//            Task buildTask = target.getRootProject().childProjects.get(extension.builderName).getTasksByName("buildPackage", true)[0]
//        }
    }

    private static void attachArtifacts(PublishExtension extension, Project project) {
        project.plugins.withId('com.android.library') {
            project.android.libraryVariants.all { variant ->
                String publicationName = variant.name
                MavenPublication publication = createPublication(publicationName, project, extension)
                PropertyFinder propertyFinder = new PropertyFinder(project, extension)
                new AndroidAttachments(publicationName, project, variant
                        , !propertyFinder.AArPath.isEmpty()).attachTo(publication)
            }
        }
        project.plugins.withId('java') {
            def mavenPublication = project.publishing.publications.find { it.name == 'maven' }
            if (mavenPublication == null) {
                String publicationName = 'maven'
                MavenPublication publication = createPublication(publicationName, project, extension)
                new JavaAttachments(publicationName, project).attachTo(publication)
            }
        }
    }

    private static MavenPublication createPublication(String publicationName, Project project, PublishExtension extension) {
        PropertyFinder propertyFinder = new PropertyFinder(project, extension)
        String groupId = extension.groupId
        String artifactId = extension.artifactId
        String version = propertyFinder.publishVersion
        String aarPath = propertyFinder.AArPath

        PublicationContainer publicationContainer = project.extensions.getByType(PublishingExtension).publications
        return publicationContainer.create(publicationName, MavenPublication) { MavenPublication publication ->
            publication.groupId = groupId
            publication.artifactId = artifactId
            publication.version = version
            if (!aarPath.isEmpty()) {
                publication.artifacts = [aarPath]
            }
        } as MavenPublication
    }
}
