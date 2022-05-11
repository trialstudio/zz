import groovy.text.SimpleTemplateEngine

def test() {
    something.run()
}

def initialize() {
    def templateEngine = new SimpleTemplateEngine()
    def renderer = { template, bindings ->
            templateEngine.createTemplate("${libraryResource template}").make(bindings).toString()
        }

    node('built-in') {
        stage('create or update jobs') {
            def teamApps = readYaml text: "${libraryResource 'team-apps.yaml'}"

            teamApps.each { team ->
                team.apps.each { app ->
                    def defaultBindings = ["team": team.name, "app": app.name]
                    switch(app.type) {
                        case "springboot": addSpringbootPipelines(app.name, renderer, defaultBindings)
                            break
                        default: echo 'type not found'
                    }
                }
                addCategorizedViewJobDsl(team.name,
                        "^(${team.apps.collect { it.name }.join('|')})-(build|deploy-to-dev|deploy-to-prod)",
                        "^(.*)-(build|deploy-to-dev|deploy-to-prod)")
            }
        }
    }
}

def addSpringbootPipelines(String app, Closure renderer, HashMap<String, String> defaultBindings) {
    addPipelineJobDsl("${app}-build", renderer("springboot.groovy", defaultBindings + ["deployToDevJob": "${app}-deploy-to-dev"]))
    addPipelineJobDsl("${app}-deploy-to-dev", renderer("argoDeployment.groovy", defaultBindings))
    addPipelineJobDsl("${app}-deploy-to-prod", renderer("argoDeployment.groovy", defaultBindings))
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