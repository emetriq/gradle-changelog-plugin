This is a simple gradle plugin creating a semiautomatic changelog by extending the Nebula release plugin.

# Gradle release plugin with changelog
Based on the nebula releas plugin (https://github.com/nebula-plugins/nebula-release-plugin)


## Extension provided
```groovy
changelog {

    /** The file containing the changelog (relative to project root) */
    String changelogFile = './changelog.md'

    /** The headline for the changes after the last release */
    String replaceToken = '## [NEXT RELEASE]'

    /** when true, the build fails when no changelog placeholder (i.e. replaceToken) exists in the changelog file */
    Boolean forceChangelog = true
    
}
```
