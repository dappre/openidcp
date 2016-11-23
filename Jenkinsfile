#!/usr/bin/env groovy

def depVersion='0.0.18'       // version of the sdk-spi-impl, on which this project depends
def update='micro'            // needs to be set here in the source
def project='openidcp'        // needs to be set here in the source
def credid='5549fdb7-4cda-4dae-890c-2c19369da699' // jenkins id for deployer key for this project
def branch='master'           // can we get this as a parameter?
def release=true              // by default false; true if parameter

def giturl="git@github.com:digital-me/${project}.git"  // NB: this is the format ssh-agent understands
def tagPrefix="${branch}-"    // maybe: branch name?

node {
    def newVersion=null

    withEnv(["PATH+MAVEN=${tool 'maven'}/bin", "JAVA_HOME=${tool 'jdk1.8.0_latest'}",]) {

        stage('Prepare environment') {
            deleteDir()
            git url: giturl
            sh "mvn clean"
            
            // ask Git for the tags that start with the tagPrefix, 
            // keep everything after the first dash
            // sort it as version numbers, reversed
            // take the first entry
            // or 0.0.12 if nothing was found
            def currVersion=sh (script: "tmp=\$(git tag -l  '${tagPrefix}*' | cut -d'-' -f2- | sort -r -V | head -n1);echo \${tmp:-'0.0.12'}", returnStdout: true).trim()
            newVersion = nextVersion(update, currVersion, release);
            echo "current version is ${currVersion}, new version will be ${newVersion}"
            sh "mvn versions:set -DnewVersion=$newVersion"
            currentBuild.displayName="#${env.BUILD_NUMBER}: ${newVersion}"
            
            // extra step 
            sh "sed -i -e 's|<version>0.0.0</version>|<version>${depVersion}</version>|' pom.xml"
            currentBuild.description="depends on : ${depVersion}"
        }
        
        stage('Build & Deploy') {     
        	def goals = 'install'; //release ? 'install sonar:sonar' : 'install';
            def buildInfo = Artifactory.newBuildInfo()
            def server = Artifactory.server('qiy-artifactory@boxtel')
            def artifactoryMaven = Artifactory.newMavenBuild()
            artifactoryMaven.tool = 'maven' // Tool name from Jenkins configuration
            artifactoryMaven.deployer releaseRepo:'Qiy', snapshotRepo:'Qiy', server: server
            artifactoryMaven.resolver releaseRepo:'libs-releases', snapshotRepo:'libs-snapshots', server: server
                    
            artifactoryMaven.run pom: 'pom.xml', goals: goals, buildInfo: buildInfo
            //junit testResults: '**/target/surefire-reports/*.xml'
            //step ([$class: 'DependencyCheckPublisher', usePreviousBuildAsReference: true])
            
            if (release) {
                sh "git tag -a '${tagPrefix}${newVersion}' -m 'Release tag by Jenkins'"
                sshagent([credid]) {
                    sh "git -c core.askpass=true push origin '${tagPrefix}${newVersion}'"
                }
            }
        }
        
        stage('Build RPM') {
            def ver = newVersion;
            def rel = '0.1';
            if (!release) {
                ver = newVersion.replace("-SNAPSHOT", "");
                rel = 'SNAPSHOT';
            }
            
            build job: 'RPM Build Webapp', parameters: [
                [$class: 'StringParameterValue', name: 'NAME', value: project],
                [$class: 'StringParameterValue', name: 'RELEASE_VERSION', value: ver],
                [$class: 'StringParameterValue', name: 'RELEASE_NUMBER', value: rel],
                [$class: 'StringParameterValue', name: 'TARGET', value: 'orion1.boxtel'],
                [$class: 'StringParameterValue', name: 'VERBOSE', value: '1']
            ]
        }
        
        stage('Deliver RPM') {
            def ver = newVersion;
            def rel = '0.1';
            if (!release) {
                ver = newVersion.replace("-SNAPSHOT", "");
                rel = 'SNAPSHOT';
            }
            build job: 'RPM Delivery Webapp', parameters: [
                [$class: 'StringParameterValue', name: 'NAME', value: project],
                [$class: 'StringParameterValue', name: 'VERSION', value: ver],
                [$class: 'StringParameterValue', name: 'RELEASE', value: rel],
                [$class: 'StringParameterValue', name: 'MODE', value: 'clean'],
                // light/purge/rollback
                // TODO [FV 20161107]: Guess depending on the developer this should be dev1, dev2 ... devn 
                [$class: 'StringParameterValue', name: 'TARGET', value: 'dev1'],
                [$class: 'StringParameterValue', name: 'TIMEOUT', value: '300'],
                [$class: 'StringParameterValue', name: 'BACKUP_NUM', value: '0'],
                [$class: 'StringParameterValue', name: 'VERBOSE', value: '1'],
                [$class: 'StringParameterValue', name: 'DRYRUN', value: 'false']
            ]
        }
    }
}

@NonCPS
def nextVersion(update, currVersion, release) {
    // println "${update} - ${currVersion}"
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
    // println result
    return release ? result : "${result}-SNAPSHOT"
}

