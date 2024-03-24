package org.example.chatgpt.data.domain.openai.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.chatgpt.data.domain.openai.model.vo.LogicCheckTypeVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleLogicEntity<T> {
    // 规则过滤结果类型
    private LogicCheckTypeVO type;
    // 规则过滤结果信息
    private String info;
    // 过滤的内容，使用泛型，更灵活的过滤指定的类型
    private T data;
    
}
