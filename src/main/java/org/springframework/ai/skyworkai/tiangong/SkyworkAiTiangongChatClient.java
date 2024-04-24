package org.springframework.ai.skyworkai.tiangong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongApi;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongChatOptions;
import org.springframework.ai.skyworkai.tiangong.util.ApiUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SkyworkAiTiangongChatClient
        extends AbstractFunctionCallSupport<SkyworkAiTiangongApi.ChatCompletionMessage, SkyworkAiTiangongApi.ChatCompletionRequest, ResponseEntity<SkyworkAiTiangongApi.ChatCompletion>>
        implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private SkyworkAiTiangongChatOptions defaultOptions;
    /**
     * Low-level Skywork AI Tiangong library.
     */
    private final SkyworkAiTiangongApi skyworkAiTiangongApi;
    private final RetryTemplate retryTemplate;

    public SkyworkAiTiangongChatClient(SkyworkAiTiangongApi skyworkAiTiangongApi) {
        this(skyworkAiTiangongApi, SkyworkAiTiangongChatOptions.builder()
                        .withModel(SkyworkAiTiangongApi.ChatModel.SKYCHAT_MEGAVERSE.getValue())
                        .withMaxToken(ApiUtils.DEFAULT_MAX_TOKENS)
                        .withDoSample(Boolean.TRUE)
                        .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                        .withTopP(ApiUtils.DEFAULT_TOP_P)
                        .build());
    }

    public SkyworkAiTiangongChatClient(SkyworkAiTiangongApi skyworkAiTiangongApi, SkyworkAiTiangongChatOptions options) {
        this(skyworkAiTiangongApi, options, null, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public SkyworkAiTiangongChatClient(SkyworkAiTiangongApi skyworkAiTiangongApi, SkyworkAiTiangongChatOptions options,
                                       FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate) {
        super(functionCallbackContext);
        Assert.notNull(skyworkAiTiangongApi, "ZhipuAiApi must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.skyworkAiTiangongApi = skyworkAiTiangongApi;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }


    @Override
    public ChatResponse call(Prompt prompt) {

        var request = createRequest(prompt, false);

        return retryTemplate.execute(ctx -> {

            ResponseEntity<SkyworkAiTiangongApi.ChatCompletion> completionEntity = this.callWithFunctionSupport(request);

            var chatCompletion = completionEntity.getBody();
            if (chatCompletion == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }

            List<Generation> generations = chatCompletion.choices()
                    .stream()
                    .map(choice -> new Generation(choice.message().content(), toMap(chatCompletion.id(), choice))
                            .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null)))
                    .toList();

            return new ChatResponse(generations);
        });
    }

    private Map<String, Object> toMap(String id, SkyworkAiTiangongApi.ChatCompletion.Choice choice) {
        Map<String, Object> map = new HashMap<>();

        var message = choice.message();
        if (message.role() != null) {
            map.put("role", message.role().name());
        }
        if (choice.finishReason() != null) {
            map.put("finishReason", choice.finishReason().name());
        }
        map.put("id", id);
        return map;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        var request = createRequest(prompt, true);

        return retryTemplate.execute(ctx -> {

            var completionChunks = this.skyworkAiTiangongApi.chatCompletionStream(request);

            // For chunked responses, only the first chunk contains the choice role.
            // The rest of the chunks with same ID share the same role.
            ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

            return completionChunks.map(chunk -> toChatCompletion(chunk)).map(chatCompletion -> {

                chatCompletion = handleFunctionCallOrReturn(request, ResponseEntity.of(Optional.of(chatCompletion)))
                        .getBody();

                @SuppressWarnings("null")
                String id = chatCompletion.id();

                List<Generation> generations = chatCompletion.choices().stream().map(choice -> {
                    if (choice.message().role() != null) {
                        roleMap.putIfAbsent(id, choice.message().role().name());
                    }
                    String finish = (choice.finishReason() != null ? choice.finishReason().name() : "");
                    var generation = new Generation(choice.message().content(),
                            Map.of("id", id, "role", roleMap.get(id), "finishReason", finish));
                    if (choice.finishReason() != null) {
                        generation = generation
                                .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null));
                    }
                    return generation;
                }).toList();
                return new ChatResponse(generations);
            });
        });
    }

    private SkyworkAiTiangongApi.ChatCompletion toChatCompletion(SkyworkAiTiangongApi.ChatCompletionChunk chunk) {
        List<SkyworkAiTiangongApi.ChatCompletion.Choice> choices = chunk.choices()
                .stream()
                .map(cc -> new SkyworkAiTiangongApi.ChatCompletion.Choice(cc.index(), cc.delta(), cc.finishReason()))
                .toList();

        return new SkyworkAiTiangongApi.ChatCompletion(chunk.id(), "chat.completion", chunk.created(), chunk.model(), choices, chunk.requestId(),null);
    }

    /**
     * Accessible for testing.
     */
    SkyworkAiTiangongApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {

        Set<String> functionsForThisRequest = new HashSet<>();

        var chatCompletionMessages = prompt.getInstructions()
                .stream()
                .map(m -> new SkyworkAiTiangongApi.ChatCompletionMessage(m.getContent(),
                        SkyworkAiTiangongApi.ChatCompletionMessage.Role.valueOf(m.getMessageType().name())))
                .toList();

        var request = new SkyworkAiTiangongApi.ChatCompletionRequest(null, chatCompletionMessages, stream);

        if (this.defaultOptions != null) {
            Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions,
                    !IS_RUNTIME_CALL);

            functionsForThisRequest.addAll(defaultEnabledFunctions);

            request = ModelOptionsUtils.merge(request, this.defaultOptions, SkyworkAiTiangongApi.ChatCompletionRequest.class);
        }

        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
                var updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions, ChatOptions.class,
                        SkyworkAiTiangongChatOptions.class);

                Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
                        IS_RUNTIME_CALL);
                functionsForThisRequest.addAll(promptEnabledFunctions);

                request = ModelOptionsUtils.merge(updatedRuntimeOptions, request,
                        SkyworkAiTiangongApi.ChatCompletionRequest.class);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
                        + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Add the enabled functions definitions to the request's tools parameter.
        if (!CollectionUtils.isEmpty(functionsForThisRequest)) {

            request = ModelOptionsUtils.merge(
                    SkyworkAiTiangongChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(),
                    request, SkyworkAiTiangongApi.ChatCompletionRequest.class);
        }

        return request;
    }

    private List<SkyworkAiTiangongApi.FunctionTool> getFunctionTools(Set<String> functionNames) {
        return this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {
            var function = new SkyworkAiTiangongApi.FunctionTool.Function(functionCallback.getDescription(),
                    functionCallback.getName(), functionCallback.getInputTypeSchema());
            return new SkyworkAiTiangongApi.FunctionTool(function);
        }).toList();
    }

    //
    // Function Calling Support
    //
    @Override
    protected SkyworkAiTiangongApi.ChatCompletionRequest doCreateToolResponseRequest(SkyworkAiTiangongApi.ChatCompletionRequest previousRequest,
                                                                                     SkyworkAiTiangongApi.ChatCompletionMessage responseMessage,
                                                                                     List<SkyworkAiTiangongApi.ChatCompletionMessage> conversationHistory) {

        // Every tool-call item requires a separate function call and a response (TOOL)
        // message.
        for (SkyworkAiTiangongApi.ChatCompletionMessage.ToolCall toolCall : responseMessage.toolCalls()) {

            var functionName = toolCall.function().name();
            String functionArguments = toolCall.function().arguments();

            if (!this.functionCallbackRegister.containsKey(functionName)) {
                throw new IllegalStateException("No function callback found for function name: " + functionName);
            }

            String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

            // Add the function response to the conversation.
            conversationHistory
                    .add(new SkyworkAiTiangongApi.ChatCompletionMessage(functionResponse, SkyworkAiTiangongApi.ChatCompletionMessage.Role.TOOL, functionName, null));
        }

        // Recursively call chatCompletionWithTools until the model doesn't call a
        // functions anymore.
        SkyworkAiTiangongApi.ChatCompletionRequest newRequest = new SkyworkAiTiangongApi.ChatCompletionRequest(previousRequest.requestId(), conversationHistory, false);
        newRequest = ModelOptionsUtils.merge(newRequest, previousRequest, SkyworkAiTiangongApi.ChatCompletionRequest.class);

        return newRequest;
    }

    @Override
    protected List<SkyworkAiTiangongApi.ChatCompletionMessage> doGetUserMessages(SkyworkAiTiangongApi.ChatCompletionRequest request) {
        return request.messages();
    }

    @SuppressWarnings("null")
    @Override
    protected SkyworkAiTiangongApi.ChatCompletionMessage doGetToolResponseMessage(ResponseEntity<SkyworkAiTiangongApi.ChatCompletion> chatCompletion) {
        return chatCompletion.getBody().choices().iterator().next().message();
    }

    @Override
    protected ResponseEntity<SkyworkAiTiangongApi.ChatCompletion> doChatCompletion(SkyworkAiTiangongApi.ChatCompletionRequest request) {
        return this.skyworkAiTiangongApi.chatCompletionEntity(request);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<SkyworkAiTiangongApi.ChatCompletion> chatCompletion) {

        var body = chatCompletion.getBody();
        if (body == null) {
            return false;
        }

        var choices = body.choices();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        return !CollectionUtils.isEmpty(choices.get(0).message().toolCalls());
    }
}
