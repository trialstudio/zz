pipeline {
    agent any
    stages {
        stage ('deploy to prod') {
            steps {
                echo 'deploying ${appName} to prod'
            }
        }
    }
}