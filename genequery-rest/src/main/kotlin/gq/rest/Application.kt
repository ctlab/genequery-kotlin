package gq.rest

import gq.rest.config.GQRestProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.core.env.Environment
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor


@SpringBootApplication
@EnableConfigurationProperties
open class Application {

    @Autowired lateinit var env: Environment

    @Bean
    open fun getGQRestProperties() = GQRestProperties(env)

    @Bean open fun validator() = LocalValidatorFactoryBean()

    @Bean
    open fun methodValidationPostProcessor(): MethodValidationPostProcessor {
        val methodValidationPostProcessor = MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validator());
        return methodValidationPostProcessor;
    }

    @Bean
    open fun messageSource(): MessageSource {
        val messageSource = ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        return messageSource;
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}