package org.springframework.ai.skyworkai.tiangong.autoconfigure;

import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.skyworkai.tiangong.SkyworkAiTiangongChatClient;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Skywork AI Tiangong Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ SkyworkAiTiangongChatProperties.class, SkyworkAiTiangongConnectionProperties.class, SensetimeAiSensenovaEmbeddingProperties.class })
@ConditionalOnClass(SkyworkAiTiangongApi.class)
public class SkyworkAiTiangongAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SkyworkAiTiangongApi sensetimeAiSensenovaApi(SkyworkAiTiangongConnectionProperties properties, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

        Assert.hasText(properties.getApiKey(), "Skywork AI Tiangong API key must be set");
        Assert.hasText(properties.getBaseUrl(), "Skywork AI Tiangong base URL must be set");

        return new SkyworkAiTiangongApi(properties.getBaseUrl(), properties.getApiKey(), restClientBuilder, responseErrorHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SkyworkAiTiangongChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public SkyworkAiTiangongChatClient sensetimeAiSensenovaChatClient(SkyworkAiTiangongApi skyworkAiTiangongApi,
                                                                      SkyworkAiTiangongChatProperties chatProperties,
                                                                      List<FunctionCallback> toolFunctionCallbacks,
                                                                      FunctionCallbackContext functionCallbackContext,
                                                                      RetryTemplate retryTemplate) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        return new SkyworkAiTiangongChatClient(skyworkAiTiangongApi, chatProperties.getOptions(), functionCallbackContext, retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public SensetimeAiSensenovaEmbeddingClient sensetimeAiSensenovaEmbeddingClient(SkyworkAiTiangongApi skyworkAiTiangongApi,
                                                                                   SensetimeAiSensenovaEmbeddingProperties embeddingProperties,
                                                                                   RetryTemplate retryTemplate) {

        return new SensetimeAiSensenovaEmbeddingClient(skyworkAiTiangongApi, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
