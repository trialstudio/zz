pipeline {
    agent any
    stages {
        stage ('build') {
            steps {
                echo 'building ${app_name}'
            }
        }
    }
}