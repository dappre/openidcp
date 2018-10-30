#!/usr/bin/env groovy

// TODO: Build in the concept of 'Re-do previous from step X' 
// in Jenkins Enterprise suite this is called a checkpoint (See http://stackoverflow.com/a/38134607/1127980) 

// Load Jenkins shared libraries to customize this project
def libLegacy = [
    remote:           'ssh://git@code.in.digital-me.nl:2222/DEVops/Jenkins.git',
    branch:           'stable',
    credentialsId:    'bot-ci-dgm-rsa',
]

library(
    identifier: "libLegacy@${libLegacy.branch}",
    retriever: modernSCM([
        $class: 'GitSCMSource',
        remote: libLegacy.remote,
        credentialsId: libLegacy.credentialsId
    ])
)

def config = updateConfig {
    update = 'micro';             // needs to be set here in the source
}

def nightly = config['nightly'];
def release = config['release'];

node {
    stage ("Build & Deploy") {
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
    stage ("Build RPM") {
        node { rpmBuildWebapp(config); }
    }
    stage ("Deliver to dev1") {
        // TODO [FV 20161107]: Guess depending on the developer this should be dev1, dev2 ... devn
        node { rpmDeliveryWebapp(config, 'dev1'); }
    }
}

if (release) {
    node { 
        stage ("Tag") {
            echo "Starting tagging"
            tagGit(config);
        }
        
        stage ("Deliver to systemtest") {
                rpmDeliveryWebapp(config, 'systemtest');
                findPromotableRPMs(config, config['newVersion']);
        }
    }
    
    stage ("Promote build") {

        try {
            timeout(time:5, unit:'DAYS') {  
                input message: "You have thouroughly tested and want to promote ${config['promoversion'][0]}", submitter: 'jenkins-admins';
                node { promoteBuild(config, config['promoversion'][0]); }  
            }
        } catch (err) {
            timeout(time:1, unit:'HOURS') {  
                input message: "Do you want to promote another RPM than (agreeing means that you did test properly)?", submitter: 'jenkins-admins'
            }
            
            def chosen;
            timeout(time:1, unit:'HOURS') {
                // TODO: [FV20170102] this will fail bc the new isn't on the script whitelist  
                ChoiceParameterDefinition choice = new ChoiceParameterDefinition('Param name', config['allpromoverions'] as String[], 'Description');
                chosen = input message: 'Select one', parameters: [choice]
            }
            node { promoteBuild(config, chosen); }
        }
    }
    
    stage ("Deliver to acceptance") {
        node { rpmDeliveryWebapp(config, 'acceptance'); }
    }
    
    stage ("Go to production") {
        timeout(time:5, unit:'DAYS') {  
            input message:"Did somebody else test this really well?";  
        }
        // TODO: [FV20170102] parallelize
        node { rpmDeliveryWebapp(config, 'production'); }
        node { rpmDeliveryWebapp(config, 'demo'); }
        // TODO: deploy to public artifactory
    }
}


