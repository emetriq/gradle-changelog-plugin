package com.emetriq.gradle.changelog

import nebula.plugin.release.ReleasePlugin
import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 * Created on 12/02/16.
 */
class ChangelogReleasePlugin extends ReleasePlugin {

    static final CHANGELOG_EXT_NAME = 'changelog'
    static final String FINALIZE_CHANGELOG_TASK = 'finalizeChangelog'
    static final String NEW_CHANGELOG_ENTRY = 'newChangelogEntry'

    Grgit git = super.git

    @Override
    void apply(Project project) {
        super.apply()

        def changelogExtension = project.extensions.create(CHANGELOG_EXT_NAME, ChangelogExtension)
        def changeLogFileName = changelogExtension.changelogFile
        def today = new Date().format("yyyy-MM-dd")
        def newVersion = project.version // version from nebula-release
        def versionHeadline = "## $newVersion  /  $today"
        def versionPlaceholder = changelogExtension.replaceToken

        // task to replace the version placeholder for the current release with the actual version number
        def finalizeChangelog = project.task(FINALIZE_CHANGELOG_TASK) << {
            def runlist = project.getGradle().getTaskGraph().getAllTasks().collect{t -> t.name}
            if( !runlist.contains('release')){
                throw new GradleException('no release task running')
            }

            def changeLogFile = new File(changeLogFileName)
            def currChangeLog = changeLogFile.text

            if (!currChangeLog.contains(versionPlaceholder)){
                throw new GradleException("no new entries in $changeLogFile")
            }

            def newChangeLog = currChangeLog.replace(versionPlaceholder, newVersion)
            changeLogFile.delete()
            new File(changeLogFileName).withWriter { w ->
                w.write(newChangeLog)
            }
            super.git.add(patterns: [changeLogFileName, '.'])
            super.git.commit(message: "changelog for $newVersion")
        }

        // task to add a new version placeholder for the next release
        def newChangelogEntry  = project.task(NEW_CHANGELOG_ENTRY) << {
            def changeLogFile = new File(changeLogFileName)
            def currChangeLog = changeLogFile.text


            def replaceWith = """|$versionPlaceholder
                         |... add new changes here!
                         |
                         |$versionHeadline""".stripMargin()

            def newChangeLog = currChangeLog.replace(newVersion, replaceWith)
            changeLogFile.delete()
            new File(changeLogFileName).withWriter { w ->
                w.write(newChangeLog)
            }
            git.add(patterns: [changeLogFileName, '.'])
            git.commit(message: "changelog placeholder added")
            git.push()
        }

        project.tasks(super.FINAL_TASK_NAME).dependsOn finalizeChangelog
        newChangelogEntry.dependsOn project.tasks(super.FINAL_TASK_NAME)
    }
}
