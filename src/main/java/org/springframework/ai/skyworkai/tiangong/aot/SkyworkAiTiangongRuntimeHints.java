package org.springframework.ai.skyworkai.tiangong.aot;

import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongApi;
import org.springframework.ai.skyworkai.tiangong.api.SkyworkAiTiangongChatOptions;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class SkyworkAiTiangongRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var mcs = MemberCategory.values();
        for (var tr : findJsonAnnotatedClassesInPackage(SkyworkAiTiangongApi.class)) {
            hints.reflection().registerType(tr, mcs);
        }
        for (var tr : findJsonAnnotatedClassesInPackage(SkyworkAiTiangongChatOptions.class)) {
            hints.reflection().registerType(tr, mcs);
        }
    }

}
