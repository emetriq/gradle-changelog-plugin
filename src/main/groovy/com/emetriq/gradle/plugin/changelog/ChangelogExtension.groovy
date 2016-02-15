package com.emetriq.gradle.plugin.changelog

/**
 * Created on 12/02/16.
 */
class ChangelogExtension {

    /** The file containing the changelog (relative to project root) */
    String changelogFile = './changelog.md'

    /** The headline for the changes after the last release */
    String replaceToken = '## [NEXT RELEASE]'

    /** when true, the build fails when no changelog placeholder exists in*/
    Boolean forceChangelog = true
}
