//import DeploymentVersions

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
    entry("springV1")
}

def entryMap() {
    return [
            "springV1": new DeploymentVersions("springBuildV1", "argoProdDeployV1", "argoDevDeployV1")
    ]
}

def entry(String deploymentType) {
    def engine = new groovy.text.SimpleTemplateEngine()

    for (version in entryMap().get(deploymentType).getVersions()) {
        def versionFile = "${version}.groovy"
        renderedScript = engine.createTemplate("${libraryResource versionFile}").make(['app_name': 'test-app']).toString()
        def scriptTxt = """
pipelineJob("${app.name}-build") {
    definition {
        cps {
            script(${renderedScript})
        }
    }
}
"""

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
}