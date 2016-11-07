#!/usr/bin/env groovy

def update='micro'
def branch='master'
def release=false
def giturl='git@github.com:digital-me/openidcp.git'
def tagPrefix='rel-'

node {
    def artifactoryMaven=null
    def newVersion=null

    withEnv(["PATH+MAVEN=${tool 'maven'}/bin", "JAVA_HOME=${tool 'jdk1.8.0_latest'}"]) {
        stage('Prepare environment') {
            deleteDir()

            def server = Artifactory.server('qiy-artifactory@boxtel')
            artifactoryMaven = Artifactory.newMavenBuild()
            artifactoryMaven.tool = 'maven' // Tool name from Jenkins configuration
            artifactoryMaven.deployer releaseRepo:'Qiy', snapshotRepo:'Qiy', server: server
            artifactoryMaven.resolver releaseRepo:'libs-releases', snapshotRepo:'libs-snapshots', server: server
            
            git url: giturl
            sh "mvn clean"
            def currVersion=sh (script: 'tmp=\$(git tag -l  "${tagPrefix}*" | cut -d\'-\' -f2- | sort -r -V | head -n1);echo \${tmp:-\'0.0.12\'}', returnStdout: true).trim()
            newVersion = nextVersion(update, currVersion);
            echo "current version is ${currVersion}, new version will be ${newVersion}"
            sh "mvn versions:set -DnewVersion=$newVersion"
            sh "sed -i -e 's|<version>0.0.0</version>|<version>0.0.13</version>|' pom.xml"
        }
        
        stage('Build & Deploy') {
            def buildInfo = Artifactory.newBuildInfo()
            artifactoryMaven.run pom: 'pom.xml', goals: 'install', buildInfo: buildInfo
            sh "git tag -a 'rel-${newVersion}' -m 'Release tag by Jenkins'"
            sshagent(['5549fdb7-4cda-4dae-890c-2c19369da699']) {
                sh "git -c core.askpass=true push origin 'rel-${newVersion}'"
            }
        }
    }
}

@NonCPS
def nextVersion(update, currVersion) {
//    println "${update} - ${currVersion}"
    if (currVersion.length() < 5)  {
        throw new IllegalArgumentException("${currVersion} is too short")
    }
    def parts = currVersion.split('\\.')
    def major = parts[0].toInteger()
    def minor = parts[1].toInteger()
    def micro = parts[2].toInteger()
    
    switch (update) {
        case 'major':
            major = 1+major;
            minor = 0;
            micro = 0;
            break;
        case 'minor':
            minor = 1+minor;
            micro = 0;
            break;
        case 'micro':
            micro = 1+micro;
            break;
        default:
            throw new IllegalArgumentException(update + " is not a valid value for update")
    }
    String result = "${major}.${minor}.${micro}";
//    println result
    return result
}

