package org.example.chatgpt.data.config;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.utils.InnerWordCharUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SensitiveWordConfig {
    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
                /*
                  stringBuilder 字符串连接器
                  chars 原始字符串
                  wordResult 当前敏感词结果
                  iWordContext 上下文
                 */
                .wordReplace((stringBuilder, chars, wordResult, iWordContext) -> {
                    String sensitiveWord = InnerWordCharUtils.getString(chars, wordResult);
                    log.info("检测到敏感词: {}", sensitiveWord);
                    if("五星红旗".equals(sensitiveWord)) {
                        stringBuilder.append("国家旗帜");
                    } else if("毛主席".equals(sensitiveWord)) {
                        stringBuilder.append("教员");
                    } else {
                        // 其他默认使用 * 代替
                        int wordLength = wordResult.endIndex() - wordResult.startIndex();
                        for(int i = 0; i < wordLength; i++) {
                            stringBuilder.append('*');
                        }
                    }
                })
                .ignoreCase(true)
                .ignoreWidth(true)
                .ignoreNumStyle(true)
                .ignoreChineseStyle(true)
                .ignoreEnglishStyle(true)
                .ignoreRepeat(false)
                .enableNumCheck(true)
                .enableEmailCheck(true)
                .enableUrlCheck(true)
                .enableWordCheck(true)
                // TODO 允许不检测的敏感词和自定义敏感词
//                .wordAllow(WordAllows.chains(WordAllows.defaults(),myWordAllow))
//                .wordDeny(WordDenys.chains(WordDenys.defaults(),myWordDeny))
                .numCheckLen(1024)
                .init();
    }
    
}
