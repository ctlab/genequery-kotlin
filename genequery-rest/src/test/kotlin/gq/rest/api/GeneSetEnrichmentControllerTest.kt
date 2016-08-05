package gq.rest.api

import gq.core.data.GQGseInfo
import gq.rest.Application
import gq.rest.GQDataRepository
import gq.rest.api.GeneSetEnrichmentController.Companion.URL
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
import java.util.*


@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(
        classes = arrayOf(Application::class),
        initializers = arrayOf(PatchEnvPropsAppCtxInitializer::class))
@WebAppConfiguration
@TestPropertySource(locations = arrayOf("/application-test.properties"))
open class GeneSetEnrichmentControllerTest {

    @Autowired
    lateinit var wac: WebApplicationContext

    @Autowired
    lateinit var repository: GQDataRepository

    lateinit var mockMvc: MockMvc

    @Before
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }


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

        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.identifiedGeneFormat", equalTo("entrez")))
                .andExpect(jsonPath("$.result.geneConversionMap.494143", equalTo(494143)))
                .andExpect(jsonPath("$.result.enrichmentResultItems", hasSize<Int>(3)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].gse", equalTo(10021)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].logAdjPvalue", closeTo(-131.88785323, 1e-5)))
                .andExpect(jsonPath("$.result.enrichmentResultItems[0].intersectionSize", equalTo(55)))
                .andExpect(jsonPath("$.result.gseToTitle.GSE10021", equalTo("Some gse.")))
                .andExpect(jsonPath("$.result.gseToTitle.GSE10245", equalTo(GQGseInfo.DEFAULT_TITLE)))
                .andExpect(jsonPath("$.result.networkClusteringGroups.0.groupId", equalTo(0)))
                .andExpect(jsonPath("$.result.networkClusteringGroups.0.moduleNames", hasSize<Int>(3)))
                .andExpect(jsonPath("$.result.networkClusteringGroups.0.moduleNames", hasItem("GSE10245_GPL570#17")))
    }

    @Test
    fun testNetworkClustering() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = repository.moduleCollection.fullNameToGQModule[Triple(10089, 96, 14)]!!.sortedEntrezIds.slice(0..70)
                .plus(repository.moduleCollection.fullNameToGQModule[Triple(10089, 96, 15)]!!.sortedEntrezIds.slice(0..70))
                .plus(repository.moduleCollection.fullNameToGQModule[Triple(10089, 96, 16)]!!.sortedEntrezIds.slice(0..70))
                .plus(repository.moduleCollection.fullNameToGQModule[Triple(10089, 96, 17)]!!.sortedEntrezIds.slice(0..50))
        requestForm.genes = queryGenes.map { it.toString() }
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hs"

        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.networkClusteringGroups.0.groupId", equalTo(0)))
                .andExpect(jsonPath("$.result.networkClusteringGroups.0.moduleNames", equalTo(listOf("GSE10089_GPL96#17"))))
                .andExpect(jsonPath("$.result.networkClusteringGroups.0.annotation", nullValue()))
                .andExpect(jsonPath("$.result.networkClusteringGroups.1.moduleNames", hasSize<Int>(2)))
                .andExpect(jsonPath("$.result.networkClusteringGroups.1.moduleNames", equalTo(listOf("GSE10089_GPL96#14","GSE10089_GPL96#15"))))
                .andExpect(jsonPath("$.result.networkClusteringGroups.1.annotation", equalTo("First	cluster")))
                .andExpect(jsonPath("$.result.networkClusteringGroups.2.moduleNames", hasSize<Int>(1)))
                .andExpect(jsonPath("$.result.networkClusteringGroups.2.moduleNames", equalTo(listOf("GSE10089_GPL96#16"))))
                .andExpect(jsonPath("$.result.networkClusteringGroups.2.annotation", equalTo("Second, cluster")))
    }

    @Test
    fun testBasicRequestDifferentSpeciesSymbolToEntrez() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("ACADM", "Acadvl", "ACAT1", "ACVR1", "SGCA", "ADSL", "aasdf")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "mm"

        mockMvc.makeRequest(URL, requestForm)
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
    fun testNoResultFound() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("__1", "__2")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "mm"

        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.result.identifiedGeneFormat", equalTo("symbol")))
                .andExpect(jsonPath("$.result.enrichmentResultItems", equalTo(Collections.EMPTY_LIST)))
                .andExpect(jsonPath("$.result.gseToTitle", equalTo(Collections.EMPTY_MAP)))
                .andExpect(jsonPath("$.result.networkClusteringGroups", equalTo(Collections.EMPTY_MAP)))
    }

    @Test
    fun testBadSpecies() {
        val requestForm = GeneSetEnrichmentController.EnrichmentRequestForm()
        val queryGenes = listOf("494143", "390916")
        requestForm.genes = queryGenes
        requestForm.speciesFrom = "hs"
        requestForm.speciesTo = "hss"

        mockMvc.makeRequest(URL, requestForm)
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

        mockMvc.makeRequest(URL, requestForm)
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

        mockMvc.makeRequest(URL, requestForm)
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

        mockMvc.makeRequest(URL, requestForm)
                .andExpect(status().is4xxClientError)
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(jsonPath("$.result", nullValue()))
                .andExpect(jsonPath("$.errors", hasSize<Int>(3)))
                .andExpect(jsonPath("$.errors", hasItem(containsString("hss"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("speciesFrom"))))
                .andExpect(jsonPath("$.errors", hasItem(containsString("genes"))))
    }
}