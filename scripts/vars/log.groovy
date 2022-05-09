import DeploymentVersions
import groovy.text.SimpleTemplateEngine

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
    node {
        def teamApps = readYaml text: "${libraryResource 'team-apps.yaml'}"

        teamApps.each { team ->
            team.apps.each { app ->
                createJobs(app.name, app.deploymentType)
            }
            def categorizedViewTxt = """
                categorizedJobsView('${team.name}') {
                    jobs {
                        regex('^(${team.apps.collect{ it.name }.join("|")})-(build|deploy-to-dev|deploy-to-prod)')
                        names('')
                    }
                    categorizationCriteria {
                        regexGroupingRule(/^(.*)-(build|deploy-to-dev|deploy-to-prod)/)
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
                """
            println categorizedViewTxt
            addJobDsl(categorizedViewTxt)
        }
    }
}

static def deploymentTypeTemplateMapping() {
    return [
            "springV1": new DeploymentVersions("springBuildV1", "argoProdDeployV1", "argoDevDeployV1"),
            "miscTaskV1": new DeploymentVersions("miscTaskBuildV1", "", "")
    ]
}

def createJobs(String appName, String deploymentType) {
    def engine = new SimpleTemplateEngine()

    def version = deploymentTypeTemplateMapping().get(deploymentType).getBuildVersion()
    if (version?.trim()) {
        def versionFile = "${version}.groovy"
        renderedScript = engine.createTemplate("${libraryResource versionFile}").make(['appName': "$appName"]).toString()
        jobDslPipeline("${appName}-build", renderedScript)
    }

    version = deploymentTypeTemplateMapping().get(deploymentType).getDeployToProdVersion()
    if (version?.trim()) {
        def versionFile = "${version}.groovy"
        renderedScript = engine.createTemplate("${libraryResource versionFile}").make(['appName': "$appName"]).toString()
        jobDslPipeline("${appName}-deploy-to-prod", renderedScript)
    }

    version = deploymentTypeTemplateMapping().get(deploymentType).getDeployToDevVersion()
    if (version?.trim()) {
//        def versionFile = "${version}.groovy"
        renderedScript = engine.createTemplate("${libraryResource "${version}.groovy"}").make(['appName': "$appName"]).toString()
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

    addJobDsl(scriptTxt)
}

def addJobDsl(String scriptTxt) {
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