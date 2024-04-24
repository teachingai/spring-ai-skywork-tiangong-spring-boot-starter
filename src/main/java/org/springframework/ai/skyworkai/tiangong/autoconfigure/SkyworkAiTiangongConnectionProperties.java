package org.springframework.ai.skyworkai.tiangong.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(SkyworkAiTiangongConnectionProperties.CONFIG_PREFIX)
public class SkyworkAiTiangongConnectionProperties extends SkyworkAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.sensetimeai.sensenova";

}
