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
        project.tasks.find { it.name == 'checkChangelog' }
    }

    def 'check task order'() {
        given:

        when:
        project.plugins.apply(ChangelogReleasePlugin)

        then:
        project.tasks.finalizeChangelog.getDependsOn().contains(project.tasks.checkChangelog)
        project.tasks.final.getDependsOn().contains(project.tasks.finalizeChangelog)
        project.tasks.final.getFinalizedBy().getDependencies(null).contains(project.tasks.newChangelogEntry)
    }

}


