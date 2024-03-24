package org.example.chatgpt.data.domain.openai.service.rule.factory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.chatgpt.data.domain.openai.annotation.LogicStrategy;
import org.example.chatgpt.data.domain.openai.service.rule.ILogicFilter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description 规则工厂
 */
@Component
public class DefaultLogicFactory {
    public Map<String, ILogicFilter> logicFilterMap = new ConcurrentHashMap<>();
    
    public DefaultLogicFactory(List<ILogicFilter> logicFilters) {
        logicFilters.forEach(logic -> {
            // 查找filter类上是否存在LogicStrategy注解，并将注解信息保存到strategy变量中
            LogicStrategy strategy = AnnotationUtils.findAnnotation(logic.getClass(), LogicStrategy.class);
            if (strategy != null) {
                logicFilterMap.put(strategy.logicModel().getCode(), logic);
            }
        });
    }
    
    /**
     * @description 开启逻辑过滤器
     */
    public Map<String, ILogicFilter> openLogicFilter() {
        return logicFilterMap;
    }
    
    /**
     * @description 规则逻辑枚举
     */
    
    @Getter
    @AllArgsConstructor
    public enum LogicModel {
        ACCESS_LIMIT("ACCESS_LIMIT", "访问次数过滤"),
        SENSITIVE_WORD("SENSITIVE_WORD", "敏感词过滤"),
        ;
        
        private String code;
        private String info;
        
    }
}
