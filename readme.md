release-changelog-plugin
========================

This is a simple gradle plugin creating a semiautomatic changelog by extending the [Nebula release plugin](https://github.com/nebula-plugins/nebula-release-plugin)

# Objective
Use this plugin helping you to maintain a changelog file. If you build a release (using the great Nebula release plugin) a new entry at the changelog.md with the current
release number and the release information you provided will be created. In case there are no release information the release will fail.

# Using the plugin

## Where to get the plugin
Right now you have to install the plugin to your local maven repository or to your own nexus. We are working on publishing this plugin to https://plugins.gradle.org/. 

## Using the plugin in your build.gradle

    buildscript {
        
        repositories {
            mavenLocal()
            jcenter()
            // add your repos here
        }
                
                
        dependencies {
            classpath 'com.emetriq.gradle:changelog-release-plugin:*'
        }
    }
    
    apply plugin: 'emetriq.changelog-release'
    
This will add a couple 

# Typical workflow



# Building


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
