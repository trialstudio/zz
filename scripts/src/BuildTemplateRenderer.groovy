import groovy.text.SimpleTemplateEngine

class BuildTemplateRenderer {
    static engine = new SimpleTemplateEngine()
    private HashMap<Environment, String> bindings
    private String template

    BuildTemplateRenderer(String template, HashMap<Environment, String> bindings) {
        this.template = template
        this.bindings = bindings
    }

    String render() {
        return engine.createTemplate(template).make(bindings).toString()
    }
}
enum Environment {
    dev,
    qa,
    staging,
    prod
}
