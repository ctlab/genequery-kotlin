package gq.rest.api

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource

class PatchEnvPropsAppCtxInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        // substitute empty path to data folder from properties for path to resource folder
        val pathToTestData = GeneSetEnrichmentControllerTest::class.java.classLoader.getResource("data/").path
        applicationContext.environment.propertySources.addFirst(
                MapPropertySource("test-properties", mapOf("gq.rest.data.path" to pathToTestData)))
    }
}