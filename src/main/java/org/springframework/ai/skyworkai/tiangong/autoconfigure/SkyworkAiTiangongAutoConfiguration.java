package org.springframework.ai.skyworkai.tiangong.autoconfigure;

import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.skyworkai.tiangong.SkyworkAiTiangongChatClient;
import org.springframework.ai.skyworkai.tiangong.SkyworkAiTiangongImageClient;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongApi;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongImageApi;
import org.springframework.beans.factory.ObjectProvider;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Skywork AI Tiangong Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ SkyworkAiTiangongChatProperties.class, SkyworkAiTiangongConnectionProperties.class, SkyworkAiTiangongImageProperties.class })
@ConditionalOnClass(SkyworkAiTiangongApi.class)
public class SkyworkAiTiangongAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SkyworkAiTiangongApi skyworkAiTiangongApi(SkyworkAiTiangongConnectionProperties connectionProperties,
                                                     SkyworkAiTiangongImageProperties chatProperties,
                                                     RestClient.Builder restClientBuilder,
                                                     ResponseErrorHandler responseErrorHandler) {

        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : connectionProperties.getBaseUrl();
        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : connectionProperties.getApiKey();
        Assert.hasText(baseUrl, "Skywork AI Tiangong base URL must be set");
        Assert.hasText(apiKey, "Skywork AI Tiangong API key must be set");

        return new SkyworkAiTiangongApi(baseUrl, apiKey, restClientBuilder, responseErrorHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SkyworkAiTiangongChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public SkyworkAiTiangongChatClient skyworkAiTiangongChatClient(SkyworkAiTiangongApi skyworkAiTiangongApi,
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
    public SkyworkAiTiangongImageApi skyworkAiTiangongImageApi(SkyworkAiTiangongConnectionProperties connectionProperties,
                                                               SkyworkAiTiangongImageProperties imageProperties,
                                                               RestClient.Builder restClientBuilder,
                                                               ResponseErrorHandler responseErrorHandler) {


        String baseUrl = StringUtils.hasText(imageProperties.getBaseUrl()) ? imageProperties.getBaseUrl() : connectionProperties.getBaseUrl();
        String apiKey = StringUtils.hasText(imageProperties.getApiKey()) ? imageProperties.getApiKey() : connectionProperties.getApiKey();
        Assert.hasText(baseUrl, "Skywork AI Tiangong base URL must be set");
        Assert.hasText(apiKey, "Skywork AI Tiangong API key must be set");

        return new SkyworkAiTiangongImageApi(baseUrl, apiKey, restClientBuilder, responseErrorHandler);
    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SkyworkAiTiangongImageProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public SkyworkAiTiangongImageClient skyworkAiTiangongImageClient(SkyworkAiTiangongConnectionProperties connectionProperties,
                                                                     SkyworkAiTiangongImageProperties imageProperties,
                                                                     SkyworkAiTiangongImageApi skyworkAiTiangongImageApi,
                                                                     ObjectProvider<RetryTemplate> retryTemplateProvider) {

        String baseUrl = StringUtils.hasText(imageProperties.getBaseUrl()) ? imageProperties.getBaseUrl() : connectionProperties.getBaseUrl();
        Assert.hasText(baseUrl, "Huawei AI Pangu base URL must be set");

        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new SkyworkAiTiangongImageClient(skyworkAiTiangongImageApi, imageProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
