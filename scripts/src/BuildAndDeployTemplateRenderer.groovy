import groovy.text.SimpleTemplateEngine

class BuildAndDeployTemplateRenderer {
    private HashMap<String, String> environmentTemplateMap
    private String buildTemplate
    private static SimpleTemplateEngine engine = new SimpleTemplateEngine()

    BuildAndDeployTemplateRenderer(String buildTemplate, HashMap<String, String> environmentTemplateMap) {
        this.buildTemplate = buildTemplate
        this.environmentTemplateMap = environmentTemplateMap
    }

    String renderBuildTemplate(HashMap<String, String> bindings, Closure templateContentRetriever) {
//        return engine.createTemplate("${libraryResource buildTemplate}").make(bindings).toString()
        return engine.createTemplate(templateContentRetriever(buildTemplate)).make(bindings).toString()
    }

    HashMap<String, String> renderEnvironmentTemplate(HashMap<String, String> bindings, Closure templateContentRetriever) {
        return environmentTemplateMap.collect {
//            return [(it.key): engine.createTemplate("${libraryResource it.value}").make(bindings + ["env": it.key]).toString()]
            return [(it.key): engine.createTemplate(templateContentRetriever(it.value)).make(bindings + ["env": it.key]).toString()]
        }.sum()
    }
}