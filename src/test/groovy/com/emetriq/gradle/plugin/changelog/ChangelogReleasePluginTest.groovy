package com.emetriq.gradle.plugin.changelog

import nebula.test.ProjectSpec
import org.ajoberstar.grgit.Grgit

/**
 * Created on 12/02/16.
 */
class ChangelogReleasePluginTest extends ProjectSpec {
    Grgit git

    def setup() {
        git = Grgit.init(dir: projectDir)
    }


    def 'find the tasks'() {
        given:

        when:
        project.plugins.apply(ChangelogReleasePlugin)

        then:
        project.tasks.find { it.name == 'newChangelogEntry' }
        project.tasks.find { it.name == 'finalizeChangelog' }
    }

    def 'check task order'() {
        given:

        when:
        project.plugins.apply(ChangelogReleasePlugin)

        then:
        project.tasks.release.getDependsOn().contains(project.tasks.finalizeChangelog)
        project.tasks.release.getFinalizedBy().getDependencies(null).contains(project.tasks.newChangelogEntry)
    }

}


