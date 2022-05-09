import groovy.text.SimpleTemplateEngine

class DeployTemplateRenderer {
    static engine = new SimpleTemplateEngine()
    private String template
    private String environment

    DeployTemplateRenderer(String template, String environment) {
        this.environment = environment
        this.template = template
    }

    String render(String appName, HashMap<String, String> bindings) {
        return ["${appName}-" engine.createTemplate(template).make(bindings).toString()]
    }
}