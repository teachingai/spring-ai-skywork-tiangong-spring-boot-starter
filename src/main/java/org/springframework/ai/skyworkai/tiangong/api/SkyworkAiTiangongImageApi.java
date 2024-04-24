package org.springframework.ai.skyworkai.tiangong.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.skyworkai.tiangong.util.ApiUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.List;

public class SkyworkAiTiangongImageApi {

    public static final String DEFAULT_IMAGE_MODEL = ImageModel.COGVIEW_3.getValue();

    private final RestClient restClient;

    /**
     * Create a new Skywork AI Tiangong Image api with base URL set to https://api.moonshot.cn
     * @param apiKey Skywork AI Tiangong apiKey.
     */
    public SkyworkAiTiangongImageApi(String apiKey) {
        this(ApiUtils.DEFAULT_BASE_URL, apiKey, RestClient.builder());
    }

    /**
     * Create a new Skywork AI Tiangong Image API with the provided base URL.
     * @param baseUrl the base URL for the Skywork AI Tiangong API.
     * @param apiKey Skywork AI Tiangong apiKey.
     * @param restClientBuilder the rest client builder to use.
     */
    public SkyworkAiTiangongImageApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder) {
        this(baseUrl, apiKey, restClientBuilder, RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
    }

    /**
     * Create a new Skywork AI Tiangong Image API with the provided base URL.
     * @param baseUrl the base URL for the Skywork AI Tiangong API.
     * @param apiKey Skywork AI Tiangong apiKey.
     * @param restClientBuilder the rest client builder to use.
     * @param responseErrorHandler the response error handler to use.
     */
    public SkyworkAiTiangongImageApi(String baseUrl, String apiKey, RestClient.Builder restClientBuilder,
                                     ResponseErrorHandler responseErrorHandler) {

        this.restClient = restClientBuilder.baseUrl(baseUrl)
                .defaultHeaders(ApiUtils.getJsonContentHeaders(apiKey))
                .defaultStatusHandler(responseErrorHandler)
                .build();
    }

    /**
     * Skywork AI Tiangong Image API model.
     * <a href="https://open.bigmodel.cn/dev/api#cogview">CogView</a>
     */
    public enum ImageModel {

        /**
         * The latest CogView model released.
         */
        COGVIEW_3("cogview-3");

        private final String value;

        ImageModel(String model) {
            this.value = model;
        }

        public String getValue() {
            return this.value;
        }

    }

    // @formatter:off
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ZhipuAiImageRequest (
            @JsonProperty("prompt") String prompt,
            @JsonProperty("model") String model,
            @JsonProperty("user_id") String user) {

        public ZhipuAiImageRequest(String prompt, String model) {
            this(prompt, model, null);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ZhipuAiImageResponse(
        @JsonProperty("created") Long created,
        @JsonProperty("data") List<Data> data,
        @JsonProperty("error") Error error) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Data(
        @JsonProperty("url") String url) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(
        @JsonProperty("code") String code,
        @JsonProperty("message") String message) {
    }
    // @formatter:onn

    public ResponseEntity<ZhipuAiImageResponse> createImage(ZhipuAiImageRequest imageRequest) {
        Assert.notNull(imageRequest, "Image request cannot be null.");
        Assert.hasLength(imageRequest.prompt(), "Prompt cannot be empty.");

        return this.restClient.post()
                .uri("/api/paas/v4/images/generations")
                .body(imageRequest)
                .retrieve()
                .toEntity(ZhipuAiImageResponse.class);
    }

}
