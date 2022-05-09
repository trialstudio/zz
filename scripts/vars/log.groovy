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
    entry("springV1", "spring-app")
}

def entryMap() {
    return [
            "springV1": new DeploymentVersions("springBuildV1", "argoProdDeployV1", "argoDevDeployV1")
    ]
}

def entry(String deploymentType, String appName) {
    def engine = new groovy.text.SimpleTemplateEngine()

    def version = entryMap().get(deploymentType).getDeployToProdVersion()
    if (version?.trim()) {
        def versionFile = "${version}.groovy"
        renderedScript = engine.createTemplate("${libraryResource versionFile}").make(['app_name': "$appName"]).toString()
        def scriptTxt = """
            pipelineJob("${appName}-build") {
                definition {
                    cps {
                        script('''${renderedScript}''')
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

//    version = entryMap().get(deploymentType).getDeployToProdVersion()
//    if (version?.trim()) {
//        def versionFile = "${version}.groovy"
//        renderedScript = engine.createTemplate("${libraryResource versionFile}").make(['app_name': 'test-app']).toString()
//        def scriptTxt = """
//            pipelineJob("${appName}-deploy-to-prod") {
//                definition {
//                    cps {
//                        script('''
//            ${renderedScript}
//            ''')
//                    }
//                }
//            }
//            """.stripIndent()
//        println scriptTxt
//
//        node {
//            jobDsl failOnSeedCollision: true,
//                    ignoreMissingFiles: false,
//                    ignoreExisting: false,
//                    removedConfigFilesAction: 'DELETE',
//                    removedJobAction: 'DELETE',
//                    removedViewAction: 'DELETE',
//                    sandbox: false,
//                    unstableOnDeprecation: true,
//                    scriptText: scriptTxt
//        }
//    }
}