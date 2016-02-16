package com.emetriq.gradle.plugin.changelog

import com.energizedwork.spock.extensions.TempDirectory
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import org.ajoberstar.grgit.Grgit

/**
 * Created on 15/02/16.
 */
class ChangelogReleaseIntegrationTest extends IntegrationSpec {

    Grgit git

    // we need a dummy "remote" repository
    @TempDirectory(clean = false)
    protected File remoteDir

    // create an git based project with a changelog.md file
    def setup() {
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
        new File(projectDir, '.gitignore') << '.gradle-test*'
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
        runTasks('final') // final is the nebula-release task

        then:
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

}
