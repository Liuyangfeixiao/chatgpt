package org.example.chatgpt.data.types.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Response<T> implements Serializable {
    private static final long serialVersionUID = -7988151926241837899L;
    private String code;
    private String info;
    
    private T data;
}
