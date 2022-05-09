pipeline {
    agent any
    stages {
        stage ('deploy to dev') {
            steps {
                echo 'deploying ${appName} to dev'
            }
        }
    }
}