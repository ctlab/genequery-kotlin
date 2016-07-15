package gq.rest.api

import gq.rest.Application
import gq.rest.RestControllerAdvice
import gq.rest.services.GeneSetEnrichmentService
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.http.MockHttpOutputMessage
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import java.nio.charset.Charset


class PatchEnvPropsAppCtxInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val pathToData = GeneSetEnrichmentControllerTest::class.java.classLoader.getResource("data/").path
        applicationContext.environment.propertySources.addFirst(
                MapPropertySource("test-properties", mapOf("gq.rest.data.path" to pathToData)))
    }

}


@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(
        classes = arrayOf(Application::class),
        initializers = arrayOf(PatchEnvPropsAppCtxInitializer::class))
@WebAppConfiguration
@TestPropertySource(locations = arrayOf("/application-test.properties"))
open class GeneSetEnrichmentControllerTest {

    @Autowired
    lateinit var geneSetEnrichmentService: GeneSetEnrichmentService

    lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        mockMvc = standaloneSetup(gq.rest.api.GeneSetEnrichmentController(geneSetEnrichmentService)).
                setControllerAdvice(RestControllerAdvice()).build();
    }

    var mappingJackson2HttpMessageConverter: HttpMessageConverter<Any> = MappingJackson2HttpMessageConverter()

    val contentType = MediaType(MediaType.APPLICATION_JSON.type, MediaType.APPLICATION_JSON.subtype, Charset.forName("utf8"));

    @Test
    fun testBasicRequest() {
        val requestForm = gq.rest.api.GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("494143", "390916", "375444", "153527", "139341", "112495", "91947", "91942", "80762",
                "80213", "79665", "64963", "64105", "55333", "55179", "51637", "51227", "51121", "29978", "27089",
                "25939", "24137", "23014", "11345", "11137", "10196", "10159", "9367", "9338", "8697", "8533", "8487",
                "8293", "7272", "7020", "6728", "6500", "6240", "6170", "5955", "5934", "5716", "5636", "5634", "5160",
                "4724", "4707", "4695", "4694", "3251", "3094", "2287", "1968", "1350", "819")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"

        mockMvc.perform(post("/perform-enrichment/").content(json(requestForm)).contentType(contentType))
                .andExpect(status().isOk)
                .andDo { handler ->
                    println(handler.response.contentAsString)
                }
                .andExpect(jsonPath("$.result.identifiedGeneFormat", equalTo("entrez")))
                .andExpect(jsonPath("$.result.geneConversionMap.494143", equalTo(494143)))
                .andExpect(jsonPath("$.result.enrichmentResultItems", hasSize<Int>(3)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].gse", equalTo(10021)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].logAdjPvalue", closeTo(-131.88785323, 1e-5)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].intersectionSize", equalTo(55)))
    }

    fun json(o: Any): String {
        val outputMessage = MockHttpOutputMessage()
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, outputMessage);
        return outputMessage.bodyAsString;
    }

    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }
    private fun <T> uninitialized(): T = null as T
}