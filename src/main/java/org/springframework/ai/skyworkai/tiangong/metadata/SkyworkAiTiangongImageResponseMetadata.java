package org.springframework.ai.skyworkai.tiangong.metadata;

import org.springframework.ai.image.ImageResponseMetadata;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongImageApi;
import org.springframework.util.Assert;

import java.util.Objects;

public class SkyworkAiTiangongImageResponseMetadata implements ImageResponseMetadata {

    private final Long created;

    public static SkyworkAiTiangongImageResponseMetadata from(SkyworkAiTiangongImageApi.ZhipuAiImageResponse ZhipuAiImageResponse) {
        Assert.notNull(ZhipuAiImageResponse, "ZhipuAiImageResponse must not be null");
        return new SkyworkAiTiangongImageResponseMetadata(ZhipuAiImageResponse.created());
    }

    protected SkyworkAiTiangongImageResponseMetadata(Long created) {
        this.created = created;
    }

    @Override
    public Long created() {
        return this.created;
    }

    @Override
    public String toString() {
        return "ZhipuAiImageResponseMetadata{" + "created=" + created + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SkyworkAiTiangongImageResponseMetadata that))
            return false;
        return Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(created);
    }

}
