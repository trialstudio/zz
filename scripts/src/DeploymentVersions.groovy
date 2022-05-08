class DeploymentVersions {
    def buildVersion
    def deployToProdVersion
    def deployToDevVersion

    DeploymentVersions(buildVersion, deployToProdVersion, deployToDevVersion) {
        this.buildVersion = buildVersion
        this.deployToProdVersion = deployToProdVersion
        this.deployToDevVersion = deployToDevVersion
    }

    def getVersions() {
        return [buildVersion, deployToProdVersion, deployToDevVersion]
    }
}