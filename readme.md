release-changelog-plugin
========================

[![Build Status](https://travis-ci.org/emetriq/gradle-changelog-plugin.svg?branch=master)](https://travis-ci.org/emetriq/gradle-changelog-plugin)

This is a simple gradle plugin creating a semiautomatic changelog on top of the [Nebula release plugin](https://github.com/nebula-plugins/nebula-release-plugin)

# Objective
This plugin helps (and forces) you to maintain a changelog file.
* In contrast to other changelog plugins, the changelog is not filled with git commit comments, but has to be maintained by a human.
* If you build a release (using the great Nebula release plugin), a new entry at the changelog.md with the current release number.
* If you forgot to update your changelog.md, the release build fails. (You can override this behavior by using the [forceChangelog](#extension-provided) flag in the extension)

# Using the plugin
The usage is the same as of the the Nebula release pugin.

## Initialization
A file named `changelog.md` has to be in the root project directory and contain this line:
```
## [NEXT RELEASE]
```
This line will be later replaced during the gradle release run by the actual release number and date.

The file can also be created with the ```initChangelog``` task. This task will fail if the file aready exists.

# Typical workflow
* maintain your `changelog.md`

```
## [NEXT RELEASE]
* Feature B added
* Bug B fixed

## 1.14.0  /  2016-02-16
* Feature A added
* Bug A fixed

... and many more release notes
```
* do a release: `gradle final` (default is 'minor release' , see the [Nebula release plugin](https://github.com/nebula-plugins/nebula-release-plugin) )
**now multiple actions are triggered:**
* the nebula release plugin determines that the new version number is 1.15.0 (based on the last version tag in git, which is v1.14.0 in our case)
* the changelog release plugin updates the changelog file and does a git commit (let's assume we have Feb 16 2016 today)

```
## 1.15.0  / 2016-02-16
* Feature B added
* Bug B fixed

## 1.14.0  /  2016-02-16
* Feature A added
* Bug A fixed

... and many more release notes
```

* the nebula release plugin tags the new git commit with the 'v1.15.0' release tag
* the changelog release plugin adds a placeholder for the next changelog entries and does a git commit / push

```
## [NEXT RELEASE]
... add new changes here!

## 1.15.0  / 2016-02-16
* Feature B added
* Bug B fixed

## 1.14.0  /  2016-02-16
* Feature A added
* Bug A fixed

... many more release notes
```


# Building

## Using the plugin in your build.gradle
Build script snippet for use in all Gradle versions:
```groovy
buildscript {

    repositories {
        maven {
          url 'https://plugins.gradle.org/m2/'
        }
        // add your repos here
    }


    dependencies {
        classpath 'com.emetriq.gradle:changelog-release-plugin:1.0.1'
    }
}

apply plugin: 'emetriq.changelog-release'
```

Build script snippet for new, incubating, plugin mechanism introduced in Gradle 2.1:
```groovy
plugins {
    id "emetriq.changelog-release" version "1.0.1"
}
```
The `changelog-release-plugin` already contains the dependency to the nebula release plugin, so you are done here with the plugin configuration.

## Extension provided
You can alter the plugin's behavior using the changelog extension:

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
