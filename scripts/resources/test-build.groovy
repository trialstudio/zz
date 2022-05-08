pipeline {
    agent any

    stages {
        stage('checkout') {
            steps {
                checkout([
                        $class: 'GitSCM',
                        branches: [[name: 'main']],
                        extensions: [[$class: 'CloneOption', depth: 1, noTags: true, reference: '', shallow: true]],
                        userRemoteConfigs: [[credentialsId: 'github', url: 'https://github.com/trialstudio/${app_name}.git']]
                ])
                writeFile file: 'Dockerfile', text: '''
                    t
                    '''
            }
        }

        stage('build') {
            steps {
                echo 'build..${app_name}'
            }
        }

        stage('test') {
            steps {
                echo 'Testing..${app_name}'
            }
        }

        stage('deploy to int') {
            steps {
                build job: '${app_name}-dev', parameters: [string(name: 'version', value: "\\$BUILD_NUMBER")]
            }
        }
    }
}
