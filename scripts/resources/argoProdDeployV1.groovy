pipeline {
    agent any
    stages {
        stage ('deploy to prod') {
            steps {
                echo 'deploying ${app_name} to prod'
            }
        }
    }
}