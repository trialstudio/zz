pipeline {
    agent any
    stages {
        stage ('deploy to dev') {
            steps {
                echo 'deploying ${app} to dev'
            }
        }
    }
}