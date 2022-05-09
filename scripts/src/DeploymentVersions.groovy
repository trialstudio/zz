class DeploymentVersions {
    def buildVersion
    def deployToProdVersion
    def deployToDevVersion
    private JobDslTemplate[] templates
//    private static HashMap deploymentTypeTemplateMapping = [
//            "springV1": new JobDslTemplate()
//    ]

    DeploymentVersions(buildVersion, deployToProdVersion, deployToDevVersion) {
        this.buildVersion = buildVersion
        this.deployToProdVersion = deployToProdVersion
        this.deployToDevVersion = deployToDevVersion
    }

//    DeploymentVersions(JobDslTemplate... templates) {
//        this.templates = templates
//    }

    def getVersions() {
        return [buildVersion, deployToProdVersion, deployToDevVersion]
    }
}