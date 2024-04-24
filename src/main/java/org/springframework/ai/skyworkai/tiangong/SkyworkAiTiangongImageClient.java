package org.springframework.ai.skyworkai.tiangong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.*;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongImageApi;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongImageOptions;
import org.springframework.ai.skyworkai.tiangong.metadata.SkyworkAiTiangongImageGenerationMetadata;
import org.springframework.ai.skyworkai.tiangong.metadata.SkyworkAiTiangongImageResponseMetadata;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.List;

public class SkyworkAiTiangongImageClient implements ImageClient {

    private final static Logger logger = LoggerFactory.getLogger(SkyworkAiTiangongImageClient.class);
    private final static SkyworkAiTiangongImageGenerationMetadata DEFAULT_METADATA =  new SkyworkAiTiangongImageGenerationMetadata("");

    private SkyworkAiTiangongImageOptions defaultOptions;

    private final SkyworkAiTiangongImageApi skyworkAiTiangongImageApi;

    public final RetryTemplate retryTemplate;

    public SkyworkAiTiangongImageClient(SkyworkAiTiangongImageApi skyworkAiTiangongImageApi) {
        this(skyworkAiTiangongImageApi, SkyworkAiTiangongImageOptions.builder()
                .withModel(SkyworkAiTiangongImageApi.DEFAULT_IMAGE_MODEL)
                .build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public SkyworkAiTiangongImageClient(SkyworkAiTiangongImageApi skyworkAiTiangongImageApi, SkyworkAiTiangongImageOptions defaultOptions,
                                        RetryTemplate retryTemplate) {
        Assert.notNull(skyworkAiTiangongImageApi, "ZhipuAiImageApi must not be null");
        Assert.notNull(defaultOptions, "defaultOptions must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        this.skyworkAiTiangongImageApi = skyworkAiTiangongImageApi;
        this.defaultOptions = defaultOptions;
        this.retryTemplate = retryTemplate;
    }

    public SkyworkAiTiangongImageOptions getDefaultOptions() {
        return this.defaultOptions;
    }

    @Override
    public ImageResponse call(ImagePrompt imagePrompt) {
        return this.retryTemplate.execute(ctx -> {

            String instructions = imagePrompt.getInstructions().get(0).getText();
            SkyworkAiTiangongImageApi.ZhipuAiImageRequest imageRequest = new SkyworkAiTiangongImageApi.ZhipuAiImageRequest(instructions,
                    SkyworkAiTiangongImageApi.DEFAULT_IMAGE_MODEL);

            if (this.defaultOptions != null) {
                imageRequest = ModelOptionsUtils.merge(this.defaultOptions, imageRequest,
                        SkyworkAiTiangongImageApi.ZhipuAiImageRequest.class);
            }

            if (imagePrompt.getOptions() != null) {
                imageRequest = ModelOptionsUtils.merge(toZhipuAiImageOptions(imagePrompt.getOptions()), imageRequest,
                        SkyworkAiTiangongImageApi.ZhipuAiImageRequest.class);
            }

            // Make the request
            ResponseEntity<SkyworkAiTiangongImageApi.ZhipuAiImageResponse> imageResponseEntity = this.skyworkAiTiangongImageApi
                    .createImage(imageRequest);

            // Convert to org.springframework.ai.model derived ImageResponse data type
            return convertResponse(imageResponseEntity, imageRequest);
        });
    }

    private ImageResponse convertResponse(ResponseEntity<SkyworkAiTiangongImageApi.ZhipuAiImageResponse> imageResponseEntity,
                                          SkyworkAiTiangongImageApi.ZhipuAiImageRequest ZhipuAiImageRequest) {
        SkyworkAiTiangongImageApi.ZhipuAiImageResponse imageApiResponse = imageResponseEntity.getBody();
        if (imageApiResponse == null) {
            logger.warn("No image response returned for request: {}", ZhipuAiImageRequest);
            return new ImageResponse(List.of());
        }

        List<ImageGeneration> imageGenerationList = imageApiResponse.data().stream().map(entry -> {
            return new ImageGeneration(new Image(entry.url(), null), DEFAULT_METADATA);
        }).toList();

        ImageResponseMetadata imageResponseMetadata = SkyworkAiTiangongImageResponseMetadata.from(imageApiResponse);
        return new ImageResponse(imageGenerationList, imageResponseMetadata);
    }

    /**
     * Convert the {@link ImageOptions} into {@link SkyworkAiTiangongImageOptions}.
     * @param runtimeImageOptions the image options to use.
     * @return the converted {@link SkyworkAiTiangongImageOptions}.
     */
    private SkyworkAiTiangongImageOptions toZhipuAiImageOptions(ImageOptions runtimeImageOptions) {
        SkyworkAiTiangongImageOptions.Builder ZhipuAiImageOptionsBuilder = SkyworkAiTiangongImageOptions.builder();
        if (runtimeImageOptions != null) {
            // Handle portable image options
            if (runtimeImageOptions.getN() != null) {
                ZhipuAiImageOptionsBuilder.withN(runtimeImageOptions.getN());
            }
            if (runtimeImageOptions.getModel() != null) {
                ZhipuAiImageOptionsBuilder.withModel(runtimeImageOptions.getModel());
            }
            if (runtimeImageOptions.getResponseFormat() != null) {
                ZhipuAiImageOptionsBuilder.withResponseFormat(runtimeImageOptions.getResponseFormat());
            }
            if (runtimeImageOptions.getWidth() != null) {
                ZhipuAiImageOptionsBuilder.withWidth(runtimeImageOptions.getWidth());
            }
            if (runtimeImageOptions.getHeight() != null) {
                ZhipuAiImageOptionsBuilder.withHeight(runtimeImageOptions.getHeight());
            }
            // Handle Skywork AI Tiangong specific image options
            if (runtimeImageOptions instanceof SkyworkAiTiangongImageOptions) {
                SkyworkAiTiangongImageOptions runtimeSkyworkAiTiangongImageOptions = (SkyworkAiTiangongImageOptions) runtimeImageOptions;
                if (runtimeSkyworkAiTiangongImageOptions.getQuality() != null) {
                    ZhipuAiImageOptionsBuilder.withQuality(runtimeSkyworkAiTiangongImageOptions.getQuality());
                }
                if (runtimeSkyworkAiTiangongImageOptions.getStyle() != null) {
                    ZhipuAiImageOptionsBuilder.withStyle(runtimeSkyworkAiTiangongImageOptions.getStyle());
                }
                if (runtimeSkyworkAiTiangongImageOptions.getUser() != null) {
                    ZhipuAiImageOptionsBuilder.withUser(runtimeSkyworkAiTiangongImageOptions.getUser());
                }
            }
        }
        return ZhipuAiImageOptionsBuilder.build();
    }

}
