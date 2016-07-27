package gq.rest.api

import gq.rest.Application
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.http.MockHttpOutputMessage
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.nio.charset.Charset


@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(
        classes = arrayOf(Application::class),
        initializers = arrayOf(PatchEnvPropsAppCtxInitializer::class))
@WebAppConfiguration
@TestPropertySource(locations = arrayOf("/application-test.properties"))
open class GeneSetEnrichmentControllerTest {

    @Autowired
    lateinit var wac: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    var mappingJackson2HttpMessageConverter: HttpMessageConverter<Any> = MappingJackson2HttpMessageConverter()

    val contentType = MediaType(MediaType.APPLICATION_JSON.type, MediaType.APPLICATION_JSON.subtype, Charset.forName("utf8"));

    @Test
    fun testBasicRequestSameSpeciesEntrezToEntrez() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("494143", "390916", "375444", "153527", "139341", "112495", "91947", "91942", "80762",
                "80213", "79665", "64963", "64105", "55333", "55179", "51637", "51227", "51121", "29978", "27089",
                "25939", "24137", "23014", "11345", "11137", "10196", "10159", "9367", "9338", "8697", "8533", "8487",
                "8293", "7272", "7020", "6728", "6500", "6240", "6170", "5955", "5934", "5716", "5636", "5634", "5160",
                "4724", "4707", "4695", "4694", "3251", "3094", "2287", "1968", "1350", "819")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"

        makeRequest(requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.identifiedGeneFormat", equalTo("entrez")))
                .andExpect(jsonPath("$.result.geneConversionMap.494143", equalTo(494143)))
                .andExpect(jsonPath("$.result.enrichmentResultItems", hasSize<Int>(3)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].gse", equalTo(10021)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].logAdjPvalue", closeTo(-131.88785323, 1e-5)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].intersectionSize", equalTo(55)))
    }

    @Test
    fun testBasicRequestDifferentSpeciesSymbolToEntrez() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("ACADM", "Acadvl", "ACAT1", "ACVR1", "SGCA", "ADSL", "aasdf")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "mm"

        makeRequest(requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.result.identifiedGeneFormat", equalTo("symbol")))
                .andExpect(jsonPath("$.result.geneConversionMap.ADSL", equalTo(11564)))
                .andExpect(jsonPath("$.result.geneConversionMap.aasdf", nullValue()))
                .andExpect(jsonPath("$.result.enrichmentResultItems", hasSize<Int>(1)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].moduleNumber", equalTo(6)))
    }

    @Test
    fun testBadSpecies() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("494143", "390916")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hss"

        makeRequest(requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(1)))
                .andExpect(jsonPath("$.errors[0]", containsString("hss")))
    }

    @Test
    fun testAmbiguousGenes() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("494143", "Abc")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"

        makeRequest(requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(1)))
                .andExpect(jsonPath("$.errors[0]", allOf(containsString("494143"), containsString("Abc"))))
    }

    @Test
    fun testNoSpecies() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("494143", "390916")
        requestForm.genes = queryGenes
        requestForm.speciesTo = "hs"

        makeRequest(requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(1)))
                .andExpect(jsonPath("$.errors[0]", containsString("speciesFrom")))
    }

    @Test
    fun testFewErrors() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        requestForm.genes = emptyList()
        requestForm.speciesTo = "hss"

        makeRequest(requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(3)))
                .andExpect(jsonPath("$.errors", hasItem(containsString("hss"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("speciesFrom"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("genes"))))
    }

    private fun makeRequest(form: GeneSetEnrichmentController.EnrichmentRequestForm): ResultActions {
        return mockMvc.perform(post(GeneSetEnrichmentController.URL).content(json(form)).contentType(contentType))
                .andDo { handler ->
                    println(handler.response.contentAsString)
                }
    }

    private fun json(o: Any): String {
        val outputMessage = MockHttpOutputMessage()
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, outputMessage);
        return outputMessage.bodyAsString;
    }
}