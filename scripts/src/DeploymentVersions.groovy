class DeploymentVersions {
    def buildVersion
    def deployToProdVersion
    def deployToDevVersion

    DeploymentVersions(buildVersion, deployToProdVersion, deployToDevVersion) {
        this.buildVersion = buildVersion
        this.deployToProdVersion = deployToProdVersion
        this.deployToDevVersion = deployToDevVersion
    }

    def print() {
        println "a$buildVersion"
        println "b$deployToProdVersion"
        println "c$deployToDevVersion"
    }
}