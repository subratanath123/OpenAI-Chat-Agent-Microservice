package net.ai.chatbot.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

//https://docs.spring.io/spring-ai/reference/api/vectordbs/pinecone.html

@Configuration
public class OpenAiConfiguration {

    @Value("${openai.api.key}")
    private String apiKey;

    /**
     * Configures and returns a RestTemplate bean with an interceptor to add an Authorization header with the API key.
     *
     * @return A RestTemplate configured with an interceptor to add an Authorization header.
     */
    @Bean
    public RestTemplate restTemplate() {

        RestTemplate restTemplate = new RestTemplate();

        restTemplate
                .getInterceptors()
                .add((request, body, execution) -> {
                    request.getHeaders().add("Authorization", "Bearer " + apiKey);
                    return execution.execute(request, body);
                });

        return restTemplate;
    }


    @Bean
    public EmbeddingModel embeddingModel() {
        return new OpenAiEmbeddingModel( OpenAiApi.builder().apiKey(apiKey).build());
    }

}
