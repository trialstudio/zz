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
            addCategorizedViewJobDsl(team.name,
            "^(${team.apps.collect { it.name }.join('|')})-(build|deploy-to-dev|deploy-to-prod)",
            "^(.*)-(build|deploy-to-dev|deploy-to-prod)")
//            def categorizedViewTxt = """
//                categorizedJobsView('${team.name}') {
//                    jobs {
//                        regex('^(${team.apps.collect{ it.name }.join("|")})-(build|deploy-to-dev|deploy-to-prod)')
//                        names('')
//                    }
//                    categorizationCriteria {
//                        regexGroupingRule(/^(.*)-(build|deploy-to-dev|deploy-to-prod)/)
//                    }
//                    columns {
//                        status()
//                        categorizedJob()
//                        lastSuccess()
//                        lastFailure()
//                        lastDuration()
//                        buildButton()
//                    }
//                }
//                """
//            addJobDsl(categorizedViewTxt)
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
        addPipelineJobDsl("${appName}-build",
                engine.createTemplate("${libraryResource "${version}.groovy"}").make(['appName': "$appName"]).toString())
    }

    version = deploymentTypeTemplateMapping().get(deploymentType).getDeployToProdVersion()
    if (version?.trim()) {
        addPipelineJobDsl("${appName}-deploy-to-prod",
                engine.createTemplate("${libraryResource "${version}.groovy"}").make(['appName': "$appName"]).toString())
    }

    version = deploymentTypeTemplateMapping().get(deploymentType).getDeployToDevVersion()
    if (version?.trim()) {
        addPipelineJobDsl("${appName}-deploy-to-dev",
                engine.createTemplate("${libraryResource "${version}.groovy"}").make(['appName': "$appName"]).toString())
    }
}

import BuildAndDeployTemplateRenderer
import DeploymentEnvironment

static def deploymentTypeTemplateMappings() {
    return [
            "springV1": new BuildAndDeployTemplateRenderer("springBuildV1.groovy", [DeploymentEnvironment.dev: "argoDeployV1.groovy", DeploymentEnvironment.prod: "argoDeployV1.groovy"])
    ]
}

def g() {
    node {
        def teamApps = readYaml text: "${libraryResource 'team-apps.yaml'}"

        teamApps.each { team ->
            team.apps.each { app ->
                def rendered = deploymentTypeTemplateMappings().get(app.deploymentType)
                def defaultBindings = ["appName": app.name, "team": team.name]
                if (rendered instanceof BuildAndDeployTemplateRenderer) {
                    def environmentTemplateMapping = rendered.renderEnvironmentTemplate(defaultBindings)
                    def extendedBindings = environmentTemplateMapping + defaultBindings

                    addPipelineJobDsl("${app.name}-build", rendered.renderBuildTemplate(extendedBindings))
                    environmentTemplateMapping.each {
                        addPipelineJobDsl("${app.name}-${it.key}", it.value)
                    }
                }
            }
            addCategorizedViewJobDsl(team.name,
                    "^(${team.apps.collect { it.name }.join('|')})-(build|deploy-to-dev|deploy-to-prod)",
                    "^(.*)-(build|deploy-to-dev|deploy-to-prod)")
        }
    }
}

def createBuildAndDeployJobs(String appName) {

}

def addBuildTemplate(String appName, String templateName, HashMap bindings) {
    new HashMap(new HashMap<String, String>())
    addPipelineJobDsl("${appName}-build", engin)
}

def addPipelineJobDsl(String jobName, String renderedPipeline) {
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

def addCategorizedViewJobDsl(String viewName, String jobRegex, String regexGroupingRule) {
    def categorizedViewTxt = """
                categorizedJobsView(/${viewName}/) {
                    jobs {
                        regex('${jobRegex}')
                        names('')
                    }
                    categorizationCriteria {
                        regexGroupingRule(/${regexGroupingRule}/)
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

    addJobDsl(categorizedViewTxt)
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