package gq.rest.api

import gq.rest.Application
import gq.rest.api.OverlapController.Companion.URL
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext


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

    @Test
    fun testBasicRequest() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("440915", "127703", "85377", "81577", "100509635", "100420758", "497661", "388403")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE1000_GPL96#0"
        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.overlapSymbolGenes", hasSize<Int>(4)))
                .andExpect(jsonPath("$.result.otherModuleSymbolGenes", hasSize<Int>(49)))
    }

    @Test
    fun testWholeModuleOverlap() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("440915", "127703", "85377", "81577")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE10271_GPL570#0"
        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.overlapSymbolGenes", hasSize<Int>(4)))
                .andExpect(jsonPath("$.result.otherModuleSymbolGenes", hasSize<Int>(0)))
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
        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.overlapSymbolGenes", hasSize<Int>(queryGenes.size)))
                .andExpect(jsonPath("$.result.otherModuleSymbolGenes", hasSize<Int>(53 - queryGenes.size)))
    }

    @Test
    fun testNoOverlap() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("1337", "42")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE1000_GPL96#0"
        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.overlapSymbolGenes", hasSize<Int>(0)))
                .andExpect(jsonPath("$.result.otherModuleSymbolGenes", hasSize<Int>(53)))
    }

    @Test
    fun testAmbiguousGenes() {
        val requestForm = OverlapController.OverlapRequestForm()
        val queryGenes = listOf("440915", "Abc")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"
        requestForm.moduleName = "GSE1000_GPL96#0"

        mockMvc.makeRequest(URL, requestForm)
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

        mockMvc.makeRequest(URL, requestForm)
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

        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(4)))
                .andExpect(jsonPath("$.errors", hasItem(containsString("moduleName"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("hss"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("speciesFrom"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("genes"))))
    }
}