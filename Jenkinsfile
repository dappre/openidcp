#!/usr/bin/env groovy

def config = updateConfig {
    update = 'micro';             // needs to be set here in the source
}

def nightly = config['nightly'];
def release = config['release'];

node {
    withEnv(["PATH+MAVEN=${tool 'maven'}/bin", "JAVA_HOME=${tool 'jdk1.8.0_latest'}"]) {
        stage ("Build") {
            echo "Cleaning dir and getting source"
            getCleanGitSource(config);
            echo "Getting latest versions of nl.** projects that we depend upon"
            updateNlMvnDependencies(config);
            echo "Getting latest version from Git and updating the pom accordingly"
            updateMvnVersionFromGitTag(config);
            echo "Building maven project and deploying to Artifactory"
            buildMvnAndDeploy(config);
            echo "Done build stage"
            
            // stash
        }
    }
    stage ("Build RPM") {
        node { rpmBuildWebapp(config); }
    }
    stage ("Deploy to dev2") {
        // TODO [FV 20161107]: Guess depending on the developer this should be dev1, dev2 ... devn
        node { rpmDeliveryWebapp(config, 'dev2'); }
    }
}

if (release) {
    stage ("Tag") {
        echo "Starting tagging"
        tagGit(config);
    }
    
    stage ("Deploy to systemtest") {
        node { rpmDeliveryWebapp(config, 'systemtest'); }
    }

    stage ("Promote build") {
    // TODO fix promote build to figure out what this needs to be
//        timeout(time:5, unit:'DAYS') {  
//            input message:"Did you test properly?";  
//        }
//        node { promoteBuild(config); }
        
        // workaround: have the user do it manually
        timeout(time:5, unit:'DAYS') {
            input message:"Did you test properly?";
        }
    }
    
    stage ("Deploy to acceptance") {
        node { rpmDeliveryWebapp(config, 'acceptance'); }
    }
    
    stage ("Deploy to production") {
        timeout(time:5, unit:'DAYS') {  
            input message:"Did somebody else test this really well?";  
        }
        node { rpmDeliveryWebapp(config, 'production'); }
        node { rpmDeliveryWebapp(config, 'demo'); }
    }
}


