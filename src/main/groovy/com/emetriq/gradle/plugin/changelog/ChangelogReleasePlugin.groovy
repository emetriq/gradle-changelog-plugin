package com.emetriq.gradle.plugin.changelog

import nebula.plugin.release.ReleasePlugin
import org.ajoberstar.gradle.git.release.base.ReleasePluginExtension
import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Created on 12/02/16.
 */
class ChangelogReleasePlugin implements Plugin<Project>  {
    static Logger logger = Logging.getLogger(ChangelogReleasePlugin)

    static final CHANGELOG_EXT_NAME = 'changelog'
    static final String FINALIZE_CHANGELOG_TASK = 'finalizeChangelog'
    static final String NEW_CHANGELOG_ENTRY = 'newChangelogEntry'

    private String newVersion = null
    private String versionHeadline = null

    Grgit git

    @Override
    void apply(Project project) {
        project.plugins.apply ReleasePlugin

        ReleasePluginExtension releaseExtension = project.extensions.findByType(ReleasePluginExtension)
        git = releaseExtension.grgit

        def changelogExtension = project.extensions.create(CHANGELOG_EXT_NAME, ChangelogExtension)
        def changeLogFileName = changelogExtension.changelogFile
        def versionPlaceholder = changelogExtension.replaceToken

        logger.debug("creating tasks")

        // task to replace the version placeholder for the current release with the actual version number
        def finalizeChangelog = project.task(FINALIZE_CHANGELOG_TASK) << {
            def runlist = project.getGradle().getTaskGraph().getAllTasks().collect { t -> t.name }
            if (!runlist.contains('release')) {
                throw new GradleException('no release task running')
            }
            def today = new Date().format("yyyy-MM-dd")
            newVersion = project.version.toString() // version from nebula-release
            versionHeadline = "## $newVersion  /  $today"

            def changeLogFile = new File(changeLogFileName)
            def currChangeLog = changeLogFile.text

            if (!currChangeLog.contains(versionPlaceholder)) {
                throw new GradleException("no new entries in $changeLogFile")
            }

            def newChangeLog = currChangeLog.replace(versionPlaceholder, versionHeadline)
            changeLogFile.delete()
            new File(changeLogFileName).withWriter { w ->
                w.write(newChangeLog)
            }
            git.add(patterns: [changeLogFileName, '.'])
            git.commit(message: "changelog for $newVersion")
        }

        // task to add a new version placeholder for the next release
        def newChangelogEntry = project.task(NEW_CHANGELOG_ENTRY) << {
            def changeLogFile = new File(changeLogFileName)
            def currChangeLog = changeLogFile.text


            def replaceWith = """|$versionPlaceholder
                         |... add new changes here!
                         |
                         |$versionHeadline""".stripMargin()

            def newChangeLog = currChangeLog.replace(versionHeadline, replaceWith)
            changeLogFile.delete()
            new File(changeLogFileName).withWriter { w ->
                w.write(newChangeLog)
            }
            git.add(patterns: [changeLogFileName, '.'])
            git.commit(message: "changelog placeholder added")
            git.push()
        }

        logger.debug("creating tasks finished")

        project.tasks.release.dependsOn finalizeChangelog
        newChangelogEntry.dependsOn project.tasks.final
    }

}


