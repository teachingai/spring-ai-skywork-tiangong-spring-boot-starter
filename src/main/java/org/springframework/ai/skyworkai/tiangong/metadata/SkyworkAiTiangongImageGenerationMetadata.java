package org.springframework.ai.skyworkai.tiangong.metadata;

import org.springframework.ai.image.ImageGenerationMetadata;

import java.util.Objects;

public class SkyworkAiTiangongImageGenerationMetadata implements ImageGenerationMetadata {

    private String revisedPrompt;

    public SkyworkAiTiangongImageGenerationMetadata(String revisedPrompt) {
        this.revisedPrompt = revisedPrompt;
    }

    public String getRevisedPrompt() {
        return revisedPrompt;
    }

    @Override
    public String toString() {
        return "ZhipuAiImageGenerationMetadata{" + "revisedPrompt='" + revisedPrompt + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkyworkAiTiangongImageGenerationMetadata that)) {
            return false;
        }
        return Objects.equals(revisedPrompt, that.revisedPrompt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(revisedPrompt);
    }

}
