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
open class OverlapControllerTest {

    @Autowired
    lateinit var wac: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    var mappingJackson2HttpMessageConverter: HttpMessageConverter<Any> = MappingJackson2HttpMessageConverter()

    val contentType = MediaType(MediaType.APPLICATION_JSON.type, MediaType.APPLICATION_JSON.subtype, Charset.forName("utf8"))

    @Test
    fun testBasicRequest() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("440915", "127703", "85377", "81577", "100509635", "100420758", "497661", "388403")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE1000_GPL96#0"
        makeRequest(requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.overlapGenes", hasSize<Int>(4)))
    }

    @Test
    fun testFullModuleOverlap() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("440915", "127703", "85377", "81577", "80339", "64794", "63035", "58513", "57157",
                "56834", "54974", "54662", "29095", "25786", "23435", "23428", "23389", "23221", "23099", "11264",
                "11187", "11034", "9922", "7804", "7184", "6992", "6772", "6388", "6386", "6233", "6181", "6166",
                "5983", "5831", "5780", "5754", "5630", "5307", "4948", "4691", "4216", "4026", "3996", "3636", "3070",
                "2869", "2512", "1984", "1933", "1793", "832", "805", "162")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE1000_GPL96#0"
        makeRequest(requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.overlapGenes", hasSize<Int>(queryGenes.size)))
    }

    @Test
    fun testNoOverlap() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("1337", "42")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE1000_GPL96#0"
        makeRequest(requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.overlapGenes", hasSize<Int>(0)))
    }

    @Test
    fun testAmbiguousGenes() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("440915", "Abc")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE1000_GPL96#0"

        makeRequest(requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(1)))
                .andExpect(jsonPath("$.errors[0]", allOf(containsString("440915"), containsString("Abc"))))
    }

    @Test
    fun testMissingModule() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("440915", "127703")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE1337_GPL42#0"

        makeRequest(requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(1)))
                .andExpect(jsonPath("$.errors[0]", allOf(containsString("Module"), containsString("not found"))))
    }

    @Test
    fun testFewErrors() {
        val requestForm = OverlapController.OverlapRequestForm()
        requestForm.genes = emptyList()
        requestForm.speciesTo = "hss"

        makeRequest(requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(4)))
                .andExpect(jsonPath("$.errors", hasItem(containsString("moduleName"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("hss"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("speciesFrom"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("genes"))))
    }

    private fun makeRequest(form: OverlapController.OverlapRequestForm): ResultActions {
        return mockMvc.perform(post(OverlapController.URL).content(json(form)).contentType(contentType))
                .andDo { handler ->
                    println(handler.response.contentAsString)
                }
    }

    private fun json(o: Any): String {
        val outputMessage = MockHttpOutputMessage()
        mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, outputMessage)
        return outputMessage.bodyAsString
    }
}