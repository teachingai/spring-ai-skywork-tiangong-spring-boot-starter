package org.springframework.ai.skyworkai.tiangong.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.image.ImageOptions;

import java.util.Objects;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkyworkAiTiangongImageOptions implements ImageOptions {

    /**
     * The number of images to generate. Must be between 1 and 10. For dall-e-3, only n=1
     * is supported.
     */
    @JsonProperty("n")
    private Integer n;

    /**
     * The model to use for image generation.
     */
    @JsonProperty("model")
    private String model;

    /**
     * The width of the generated images. Must be one of 256, 512, or 1024 for dall-e-2.
     */
    @JsonProperty("size_width")
    private Integer width;

    /**
     * The height of the generated images. Must be one of 256, 512, or 1024 for dall-e-2.
     */
    @JsonProperty("size_height")
    private Integer height;

    /**
     * The quality of the image that will be generated. hd creates images with finer
     * details and greater consistency across the image. This param is only supported for
     * dall-e-3.
     */
    @JsonProperty("quality")
    private String quality;

    /**
     * The format in which the generated images are returned. Must be one of url or
     * b64_json.
     */
    @JsonProperty("response_format")
    private String responseFormat;

    /**
     * The size of the generated images. Must be one of 256x256, 512x512, or 1024x1024 for
     * dall-e-2. Must be one of 1024x1024, 1792x1024, or 1024x1792 for dall-e-3 models.
     */
    @JsonProperty("size")
    private String size;

    /**
     * The style of the generated images. Must be one of vivid or natural. Vivid causes
     * the model to lean towards generating hyper-real and dramatic images. Natural causes
     * the model to produce more natural, less hyper-real looking images. This param is
     * only supported for dall-e-3.
     */
    @JsonProperty("style")
    private String style;

    /**
     * A unique identifier representing your end-user, which can help Skywork AI Tiangong to monitor
     * and detect abuse.
     */
    @JsonProperty("user")
    private String user;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final SkyworkAiTiangongImageOptions options;

        private Builder() {
            this.options = new SkyworkAiTiangongImageOptions();
        }

        public Builder withN(Integer n) {
            options.setN(n);
            return this;
        }

        public Builder withModel(String model) {
            options.setModel(model);
            return this;
        }

        public Builder withQuality(String quality) {
            options.setQuality(quality);
            return this;
        }

        public Builder withResponseFormat(String responseFormat) {
            options.setResponseFormat(responseFormat);
            return this;
        }

        public Builder withWidth(Integer width) {
            options.setWidth(width);
            return this;
        }

        public Builder withHeight(Integer height) {
            options.setHeight(height);
            return this;
        }

        public Builder withStyle(String style) {
            options.setStyle(style);
            return this;
        }

        public Builder withUser(String user) {
            options.setUser(user);
            return this;
        }

        public SkyworkAiTiangongImageOptions build() {
            return options;
        }

    }

    @Override
    public Integer getN() {
        return this.n;
    }

    public void setN(Integer n) {
        this.n = n;
    }

    @Override
    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getQuality() {
        return this.quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    @Override
    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    @Override
    public Integer getWidth() {
        return this.width;
    }

    public void setWidth(Integer width) {
        this.width = width;
        this.size = this.width + "x" + this.height;
    }

    @Override
    public Integer getHeight() {
        return this.height;
    }

    public void setHeight(Integer height) {
        this.height = height;
        this.size = this.width + "x" + this.height;
    }

    public String getStyle() {
        return this.style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {

        if (this.size != null) {
            return this.size;
        }
        return (this.width != null && this.height != null) ? this.width + "x" + this.height : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SkyworkAiTiangongImageOptions that))
            return false;
        return Objects.equals(n, that.n) && Objects.equals(model, that.model) && Objects.equals(width, that.width)
                && Objects.equals(height, that.height) && Objects.equals(quality, that.quality)
                && Objects.equals(responseFormat, that.responseFormat) && Objects.equals(size, that.size)
                && Objects.equals(style, that.style) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(n, model, width, height, quality, responseFormat, size, style, user);
    }

    @Override
    public String toString() {
        return "ZhipuAiImageOptions{" + "n=" + n + ", model='" + model + '\'' + ", width=" + width + ", height=" + height
                + ", quality='" + quality + '\'' + ", responseFormat='" + responseFormat + '\'' + ", size='" + size
                + '\'' + ", style='" + style + '\'' + ", user='" + user + '\'' + '}';
    }

}
