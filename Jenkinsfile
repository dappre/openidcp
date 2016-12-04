#!/usr/bin/env groovy

// This file is used in multi branch jenkins build, so this will build the snapshot.
// With multi branch builds we cannot set parameters.

def config = [
    // build/feature specific (most likely to change)
    release: true,             // by default false; true if parameter
    depVersion: '0.0.19', // version of the sdk-lib, on which this project depends
    update: "micro",            // needs to be set here in the source
    
    // project specific settings
    project: "openidcp",      // needs to be set here in the source
    credid: "5549fdb7-4cda-4dae-890c-2c19369da699", // jenkins id for deployer key for this project
    
    // calculated settings
    branch: 'master',           // can we get this as a parameter?
    giturl: null,
    newVersion: null
];


multibranchGitHub(env.BRANCH_NAME, config);

println config

node {
    withEnv(["PATH+MAVEN=${tool 'maven'}/bin", "JAVA_HOME=${tool 'jdk1.8.0_latest'}"]) {
        getCleanSource(this, config);
        setNewVersion(this, config);
        buildAndDeploy(this, config, []);
        tagRelease(this, config);
        buildRPM(this, config);
        deployRPM(this, config, "dev1")
        
    }
}

// all below this should be moved to some library


def multibranchGitHub(branch, config) {
    config['giturl'] = "git@github.com:digital-me/${config['project']}.git" // NB: this is the format ssh-agent understands
    
    if (branch != null) {
        println "branch: ${branch}"
        config['branch']=branch;
    } else {
        println "not a multibranch, branch is master"
    }
    
}

def deployRPM(script, config, env) {
    stage("Deliver to ${env}") {
        def ver = newVersion;
        def rel = '0.1';
        if (!release) {
            ver = newVersion.replace("-SNAPSHOT", "");
            rel = 'SNAPSHOT';
        }
        build job: 'RPM Delivery Webapp', parameters: [
            [$class: 'StringParameterValue', name: 'NAME', value: config['project']],
            [$class: 'StringParameterValue', name: 'VERSION', value: ver],
            [$class: 'StringParameterValue', name: 'RELEASE', value: rel],
            [$class: 'StringParameterValue', name: 'MODE', value: 'clean'],
            // light/purge/rollback
            // TODO [FV 20161107]: Guess depending on the developer this should be dev1, dev2 ... devn
            [$class: 'StringParameterValue', name: 'TARGET', value: env],
            [$class: 'StringParameterValue', name: 'TIMEOUT', value: '300'],
            [$class: 'StringParameterValue', name: 'BACKUP_NUM', value: '0'],
            [$class: 'StringParameterValue', name: 'VERBOSE', value: '1'],
            [$class: 'StringParameterValue', name: 'DRYRUN', value: 'false']
        ]
    }
}

def buildRPM(script, config) {
    script.stage('Build RPM') {
        def ver = config['newVersion'];
        def rel = '0.1';
        if (!config['release']) {
            ver = config['newVersion'].replace("-SNAPSHOT", "");
            rel = 'SNAPSHOT';
        }
        
        build job: 'RPM Build Webapp', parameters: [
            [$class: 'StringParameterValue', name: 'NAME', value: config['project']],
            [$class: 'StringParameterValue', name: 'RELEASE_VERSION', value: ver],
            [$class: 'StringParameterValue', name: 'RELEASE_NUMBER', value: rel],
            [$class: 'StringParameterValue', name: 'TARGET', value: 'orion1.boxtel'],
            [$class: 'StringParameterValue', name: 'VERBOSE', value: '1']
        ]
    }
}


def tagRelease(script, config) {
    script.stage('Tag release') {
        if (config['release']) {
            def ver = "${config['branch']}-${config['newVersion']}"
            script.sh "git tag -a '${ver}' -m 'Release tag by Jenkins'"
            script.sshagent([config['credid']]) {
                script.sh "git -c core.askpass=true push origin '${ver}'"
            }
        }
    }

}

def buildAndDeploy(script, config, options) {
    stage('Build & Deploy') {
        def goals = 'install';
        if (options.contains('nightly')) {
            goals = "${goals} org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar org.owasp:dependency-check-maven:check";
        }
        def depCheck = goals.contains("org.owasp:dependency-check-maven");
        def buildInfo = Artifactory.newBuildInfo()
        def server = Artifactory.server('private-repo')
        def artifactoryMaven = Artifactory.newMavenBuild()
        artifactoryMaven.tool = 'maven' // Tool name from Jenkins configuration
        if (depCheck) {
            artifactoryMaven.opts = "-Djava.io.tmpdir=/opt/tmp"
        }
        artifactoryMaven.deployer releaseRepo:'Qiy', snapshotRepo:'Qiy', server: server
        artifactoryMaven.resolver releaseRepo:'libs-releases', snapshotRepo:'libs-snapshots', server: server
        artifactoryMaven.run pom: 'pom.xml', goals: goals, buildInfo: buildInfo
        junit testResults: '**/target/surefire-reports/*.xml'
        if (depCheck) {
            script.step ([$class: 'DependencyCheckPublisher'])
        }
    }

}

/**
 * Calculates the next version number based in the following steps:
 * - ask Git for the tags that start with the branch name
 * - if nothing was found, use 'master' as branch name and ask again
 * - keep everything after the first dash
 * - sort it as version numbers, reversed
 * - take the first entry
 * - or 0.0.12 if nothing was found
 * - add one to either the major, minor or micro parts of the version, depending on the parameter 'update'
 * - set the versions 'to the right' (e.g. micro if the update was minor) to 0
 * - add '-SNAPSHOT' if release is false
 * - return the result
 */
def setNewVersion(script, config) {
    script.stage ('Set new version') {
        def branch = config['branch'];
        def update = config['update'];
        def release = config['release'];

        try {
            println "calculating next version ${branch} - ${update} - ${release}"
            def currVersion = script.sh (script: "git tag -l  'master-*' | cut -d'-' -f2- | sort -r -V | head -n1", returnStdout: true).trim()
            if (currVersion == null || currVersion == "") {
                currVersion = "0.0.12"
            }
            
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
            if (!'master'.equals(branch)) {
                result = "${result}-${branch}";
            }
            if (!release) {
                result = "${result}-SNAPSHOT";
            }
            //    println result
            config['newVersion'] = result;
        } catch (Throwable t) {
            println("Exception caught ${t}");
            throw t;
        }
    
        println "new version will be ${config['newVersion']}"
        script.currentBuild.displayName="#${script.env.BUILD_NUMBER}: ${config['newVersion']}"
        script.sh "mvn versions:set -DnewVersion=${config['newVersion']}"
        
        if (config['depVersion'] != null) {
            def depVersion = config['depVersion'];
            // extra step
            sh "sed -i -e 's|<version>0.0.0</version>|<version>${depVersion}</version>|' pom.xml"
            script.currentBuild.description="depends on : ${depVersion}"
        }
    }
}


def getCleanSource(script, config) {
    script.stage('Get clean source') {
        script.deleteDir()
        script.git url: config['giturl'], branch: config['branch']
    }
}


