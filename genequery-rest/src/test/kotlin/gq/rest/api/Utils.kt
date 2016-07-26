package gq.rest.api

import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.http.MockHttpOutputMessage
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.nio.charset.Charset

val mappingJackson2HttpMessageConverter: HttpMessageConverter<Any> = MappingJackson2HttpMessageConverter()

val contentType = MediaType(MediaType.APPLICATION_JSON.type, MediaType.APPLICATION_JSON.subtype, Charset.forName("utf8"))

fun MockMvc.makeRequest(url: String, form: Any): ResultActions {
    return perform(MockMvcRequestBuilders.post(url).content(json(form)).contentType(contentType))
            .andDo { handler ->
                println(handler.response.contentAsString)
            }
}

fun json(o: Any): String {
    val outputMessage = MockHttpOutputMessage()
    mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, outputMessage)
    return outputMessage.bodyAsString
}
