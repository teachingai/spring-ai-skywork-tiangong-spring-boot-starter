package org.springframework.ai.skyworkai.tiangong.autoconfigure;

import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongApi;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongChatOptions;
import org.springframework.ai.skyworkai.tiangong.util.ApiUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(SkyworkAiTiangongChatProperties.CONFIG_PREFIX)
public class SkyworkAiTiangongChatProperties extends SkyworkAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.sensetimeai.sensenova.chat";


    /**
     * Enable Skywork AI Tiangong chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever Skywork AI Tiangong options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private SkyworkAiTiangongChatOptions options = SkyworkAiTiangongChatOptions.builder()
            .withModel(SkyworkAiTiangongApi.ChatModel.SKYCHAT_MEGAVERSE.getValue())
            .withMaxToken(ApiUtils.DEFAULT_MAX_TOKENS)
            .withDoSample(Boolean.TRUE)
            .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
            .withTopP(ApiUtils.DEFAULT_TOP_P)
            .build();

    public SkyworkAiTiangongChatOptions getOptions() {
        return this.options;
    }

    public void setOptions(SkyworkAiTiangongChatOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
