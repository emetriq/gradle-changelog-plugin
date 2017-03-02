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

import com.energizedwork.spock.extensions.TempDirectory
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import org.ajoberstar.grgit.Grgit

/**
 * Created on 15/02/16.
 */
class ChangelogInitIntegrationTest extends IntegrationSpec {

    Grgit git

    // we need a dummy "remote" repository
    @TempDirectory(clean = false)
    protected File remoteDir

    // create an git based project with a changelog.md file
    def setup() {
        Grgit.init(dir: remoteDir, bare: true)

        git = Grgit.init(dir: projectDir)
        git.remote.add(name: 'origin', url: remoteDir)
        new File(projectDir, '.gitignore') << '.gradle-test*'
        git.add(patterns: ["."])
        git.commit(message: 'initial commit')
        git.tag.add(name: '0.2.0')
        git.push()
        buildFile << '''
            apply plugin: 'emetriq.changelog-release'
        '''.stripIndent()
    }

    def cleanup() {
        remoteDir.listFiles().each { file ->
            if (file.isDirectory()) {
                file.deleteDir()
            } else {
                file.delete()
            }
        }
        new File(projectDir, "changelog.md").delete()
    }

    def 'run changelog initialization task'() {
        given:

        when:
        def result = runTasks('initChangelog') // final is the nebula-release task
        println result.standardError

        then:
        def changelog = new File(projectDir, "changelog.md")
        changelog.exists()
        changelog.text.startsWith('## [NEXT RELEASE]\n... add new changes here!')
    }

    def 'init fails if file already exists'() {
        given:
        new File(projectDir, "changelog.md").createNewFile()

        when:
        def result = runTasks('initChangelog') // final is the nebula-release task

        then:
        !result.success
    }
}
