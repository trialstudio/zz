import groovy.text.SimpleTemplateEngine

class BuildAndDeployTemplateRenderer {
    private HashMap<String, String> environmentTemplateMap
    private String buildTemplate
    private static SimpleTemplateEngine engine = new SimpleTemplateEngine()

    BuildAndDeployTemplateRenderer(String buildTemplate, HashMap<String, String> environmentTemplateMap) {
        this.buildTemplate = buildTemplate
        this.environmentTemplateMap = environmentTemplateMap
    }

    String renderBuildTemplate(HashMap<String, String> bindings) {
        return engine.createTemplate("${libraryResource buildTemplate}").make(bindings).toString()
    }

    List<HashMap<String, String>> renderEnvironmentTemplate(HashMap<String, String> bindings) {
        return environmentTemplateMap.collect {
            return [(it.key): engine.createTemplate("${libraryResource it.value}").make(bindings + ["env": it.key]).toString()]
        }
    }
}