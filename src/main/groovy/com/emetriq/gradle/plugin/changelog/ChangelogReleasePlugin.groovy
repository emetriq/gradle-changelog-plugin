/*
 *  Copyright 2016 emetriq GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
class ChangelogReleasePlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(ChangelogReleasePlugin)

    static final String CHECK_CHANGELOG = 'checkChangelog'
    static final CHANGELOG_EXT_NAME = 'changelog'
    static final String FINALIZE_CHANGELOG_TASK = 'finalizeChangelog'
    static final String NEW_CHANGELOG_ENTRY = 'newChangelogEntry'
    static final NEXT_RELEASE_TEXT = '... add new changes here!'


    private String newVersion = null
    private String versionHeadline = null

    Grgit git

    @Override
    void apply(Project project) {
        project.plugins.apply ReleasePlugin

        ReleasePluginExtension releaseExtension = project.extensions.findByType(ReleasePluginExtension)
        git = releaseExtension.grgit

        def changelogExtension = project.extensions.create(CHANGELOG_EXT_NAME, ChangelogPluginExtension)

        // extension properties as closures, so they are evaluated at task runtime
        def changeLogFileName = { changelogExtension.changelogFile }
        def versionPlaceholder = { changelogExtension.replaceToken }
        def forceChangelog = { changelogExtension.forceChangelog }

        def changeLogFile = { new File(project.projectDir, changeLogFileName()) }
        def overwriteChangelog = { text ->
            changeLogFile().newWriter().withWriter { w -> w << text }
        }

        def checkChangelog = project.task(CHECK_CHANGELOG).doFirst( {
            if (project.gradle.taskGraph.hasTask(":final")
                    || project.gradle.taskGraph.hasTask(":${project.name}:final")
                    || project.gradle.startParameter.taskNames.contains(CHECK_CHANGELOG)) {
                def currentChangeLog = changeLogFile().text
                if ((!currentChangeLog.contains(versionPlaceholder()) || currentChangeLog.readLines().take(2).contains(NEXT_RELEASE_TEXT))
                        && forceChangelog()) {
                    throw new GradleException("no new entries in ${changeLogFileName()}")
                }
            }
        })

        // task to replace the version placeholder for the current release with the actual version number
        def finalizeChangelog = project.task(FINALIZE_CHANGELOG_TASK).doLast {
            def today = new Date().format("yyyy-MM-dd")
            newVersion = project.version.toString() // version from nebula-release
            versionHeadline = "## $newVersion  /  $today"

            def currentChangeLog = changeLogFile().text

            if ((!currentChangeLog.contains(versionPlaceholder()) || currentChangeLog.readLines().take(2).contains(NEXT_RELEASE_TEXT))
                    && forceChangelog()) {
                throw new GradleException("no new entries in ${changeLogFileName()}")
            } else {
                def newChangeLog = currentChangeLog.replace(versionPlaceholder(), versionHeadline)

                overwriteChangelog(newChangeLog)
                git.add(patterns: [changeLogFileName(), '.'])
                git.commit(message: "changelog for $newVersion")
                logger.info("changelog finalized")
            }
        }

        // task to add a new version placeholder for the next release
        def newChangelogEntry = project.task(NEW_CHANGELOG_ENTRY).doLast {
            def releaseChangeLog = changeLogFile().text

            def replaceWith = """|${versionPlaceholder()}
                         |$NEXT_RELEASE_TEXT
                         |
                         |$versionHeadline""".stripMargin()

            def newChangeLog = releaseChangeLog.replace(versionHeadline, replaceWith)

            if (releaseChangeLog != newChangeLog) {
                overwriteChangelog(newChangeLog)

                git.add(patterns: [changeLogFileName(), '.'])
                git.commit(message: "changelog placeholder added")
                git.push()
                logger.info("new changelog placeholder added")
            } else {
                logger.warn("no changes in ${changeLogFileName()}")
            } // no changes
        }

        //project.tasks.build.dependsOn checkChangelog
        project.tasks.final.dependsOn finalizeChangelog
        project.tasks.final.finalizedBy newChangelogEntry
    }

}


