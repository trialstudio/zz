import groovy.text.SimpleTemplateEngine

class JobDslTemplate {
    String jobName
    String template
    HashMap<String, String> bindings
    static engine = new SimpleTemplateEngine()

    JobDslTemplate(String jobName, String template, LinkedHashMap<String, String> bindings) {
        this.jobName = jobName
        this.template = template
        this.bindings = bindings
    }

    def createPipelineJob() {
        def renderedPipeline = engine.createTemplate(template).make(bindings).toString()
        def scriptTxt = """
            pipelineJob('$jobName') {
                definition {
                    cps {
                        script('''${renderedPipeline}''')
                    }
                }
            }
            """

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
