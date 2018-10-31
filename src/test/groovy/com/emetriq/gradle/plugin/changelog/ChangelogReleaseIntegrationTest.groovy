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

import nebula.test.IntegrationSpec
import org.ajoberstar.grgit.Grgit

/**
 * Created on 15/02/16.
 */
class ChangelogReleaseIntegrationTest extends IntegrationSpec {

    Grgit git

    // we need a dummy "remote" repository
    protected File remoteDir = new File("build/test").getCanonicalFile()

    // create an git based project with a changelog.md file
    def setup() {
        remoteDir.mkdirs()
        Grgit.init(dir: remoteDir, bare: true)

        git = Grgit.init(dir: projectDir)
        File changelog = new File(projectDir, 'changelog.md')
        changelog << '''## [NEXT RELEASE]
                    |... Major improvements
                    |
                    |## 0.2.0  /  2016-02-12
                    |* Bugfixes from hell
                    '''.stripMargin()
        git.remote.add(name: 'origin', url: remoteDir)
        new File(projectDir, '.gitignore') << '.gradle-test*' << '\n' << '.gradle'
        git.add(patterns: ["."])
        git.commit(message: 'initial commit')
        git.tag.add(name: '0.2.0')
        git.push()
    }

    def cleanup() {
        remoteDir.listFiles().each { file ->
            if (file.isDirectory()) {
                file.deleteDir()
            } else {
                file.delete()
            }
        }
    }

    def 'run release task'() {
        given:
        buildFile << '''
            apply plugin: 'emetriq.changelog-release'
        '''.stripIndent()
        git.add(patterns: ["."])
        git.commit(message: 'added a cool plugin')

        when:
        def result = runTasks('final') // final is the nebula-release task

        then:
        result.success

        File changelog = new File(projectDir, "changelog.md")
        changelog.text.contains('0.3.0')
        changelog.text.startsWith('## [NEXT RELEASE]\n... add new changes here!')
    }

    def 'forceChangelog flag test'() {
        given:
        buildFile << '''
            apply plugin: 'emetriq.changelog-release'

            changelog {
                forceChangelog = false
                replaceToken = '## [please don't replace me]'
            }

        '''.stripIndent()
        git.add(patterns: ["."])
        git.commit(message: 'added a cool plugin')

        when:
        runTasks('final') // final is the nebula-release task

        then:
        // no replacement expected
        File changelog = new File(projectDir, "changelog.md")
        !changelog.text.contains('0.3.0')
        changelog.text.startsWith('## [NEXT RELEASE]\n... Major improvements')
    }

    def 'snapshot release: no changelog'() {
        given:
        buildFile << '''
            apply plugin: 'emetriq.changelog-release'
        '''.stripIndent()
        git.add(patterns: ["."])
        git.commit(message: 'added a cool plugin')

        when:
        runTasks('snapshot')

        then:
        // no replacement expected
        File changelog = new File(projectDir, "changelog.md")
        !changelog.text.contains('0.3.0')
        changelog.text.startsWith('## [NEXT RELEASE]\n... Major improvements')
    }

}
