package net.ai.chatbot.config;

import net.ai.chatbot.override.OverridedOpenAiEmbeddingModel;
import net.ai.chatbot.service.pinnecone.PineconeVectorStoreFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.springframework.ai.document.MetadataMode.ALL;
import static org.springframework.ai.document.MetadataMode.EMBED;


//https://docs.spring.io/spring-ai/reference/api/vectordbs/pinecone.html
@Configuration
public class PineconeConfig {

    @Value("${spring.ai.vectorstore.pinecone.apiKey}")
    private String pinneconeApiKey;
                                                         
    @Value("${spring.ai.vectorstore.pinecone.projectId}")
    private String pinneconeProjectId;

    @Value("${spring.ai.vectorstore.pinecone.environment}")
    private String pinneconeEnvironment;

    @Value("${spring.ai.vectorstore.pinecone.index-name}")
    private String pinneconeIndexName;

    @Value("${spring.ai.vectorstore.pinecone.namespace.prefix}")
    private String pinneconeNamespace;

    @Value("${spring.ai.openai.api-key}")
    private String openAiKey;

    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Bean
    public EmbeddingModel embeddingModel() {
        OpenAiApi api = OpenAiApi.builder()
                .apiKey(openAiKey)
                .baseUrl(openAiBaseUrl)
                .build();

        OpenAiEmbeddingOptions options =  OpenAiEmbeddingOptions
                .builder()
                .model("text-embedding-3-small")
                .build();

        return new OverridedOpenAiEmbeddingModel(api, EMBED, options);
    }

    @Bean
    public VectorStore pineconeVectorStore(EmbeddingModel embeddingModel) {
        return PineconeVectorStore.builder(embeddingModel)
                .apiKey(pinneconeApiKey)
                .indexName(pinneconeIndexName)
                .namespace(pinneconeNamespace) // the free tier doesn't support namespaces.
//                .contentFieldName(CUSTOM_CONTENT_FIELD_NAME) // optional field to store the original content. Defaults to `document_content`
                .build();
    }

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
                    request.getHeaders().add("Authorization", "Bearer " + openAiKey);
                    return execution.execute(request, body);
                });

        return restTemplate;
    }

    @Bean
    public PineconeVectorStoreFactory pineconeVectorStoreFactory(EmbeddingModel embeddingModel) {
        return new PineconeVectorStoreFactory(pinneconeApiKey, pinneconeIndexName, embeddingModel);
    }

}
