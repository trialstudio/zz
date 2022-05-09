import DeploymentVersions

def info() {
    echo 'infos'
    echo "${libraryResource 'test.json'}"
}

def jobDslUtil(String appName) {
    node {
        for (buildType in ['build', 'dev-promotion', 'prod-promotion'])
            pipelineJob("${app.name}-${buildType}") {
                definition {
                    cps {
                        script(readFileFromWorkspace('pipeline.groovy'))
                    }
                }
            }
        categorizedJobsView('example') {
            jobs {
                team.apps.each {
                    name(it.name)
                }
            }
            categorizationCriteria {
                regexGroupingRule(/^(.*)-(ci|dev-promotion|prod-promotion)/)
            }
            columns {
                status()
                categorizedJob()
                lastSuccess()
                lastFailure()
                lastDuration()
                buildButton()
            }
        }
    }
}

def t() {
    entry("springV1", "spring-app")
}

def entryMap() {
    return [
            "springV1": new DeploymentVersions("springBuildV1", "argoProdDeployV1", "argoDevDeployV1")
    ]
}

def entry(String deploymentType, String appName) {
    def engine = new groovy.text.SimpleTemplateEngine()

    println 'build'
    def version = entryMap().get(deploymentType).getBuildVersion()
    if (version?.trim()) {
        def versionFile = "${version}.groovy"
        renderedScript = engine.createTemplate("${libraryResource versionFile}").make(['appName': "$appName"]).toString()
        jobDslPipeline("${appName}-build", renderedScript)
    }

    println 'prod'
    version = entryMap().get(deploymentType).getDeployToProdVersion()
    if (version?.trim()) {
        def versionFile = "${version}.groovy"
        renderedScript = engine.createTemplate("${libraryResource versionFile}").make(['appName': "$appName"]).toString()
        jobDslPipeline("${appName}-deploy-to-prod", renderedScript)
    }

    println 'dev'
    version = entryMap().get(deploymentType).getDeployToDevVersion()
    if (version?.trim()) {
        def versionFile = "${version}.groovy"
        renderedScript = engine.createTemplate("${libraryResource versionFile}").make(['appName': "$appName"]).toString()
        jobDslPipeline("${appName}-deploy-to-dev", renderedScript)
    }
}

def jobDslPipeline(String jobName, String renderedPipeline) {
    def scriptTxt = """
            pipelineJob('$jobName') {
                definition {
                    cps {
                        script('''${renderedPipeline}''')
                    }
                }
            }
            """
    println scriptTxt

    node {
        jobDsl failOnSeedCollision: true,
                ignoreMissingFiles: false,
                ignoreExisting: false,
                removedConfigFilesAction: 'DELETE',
                removedJobAction: 'DELETE',
                removedViewAction: 'DELETE',
                sandbox: false,
                unstableOnDeprecation: true,
                scriptText: scriptTxt
    }
}