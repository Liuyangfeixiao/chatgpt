package org.example.chatgpt.data;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChatgptDataAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatgptDataAppApplication.class, args);
    }

}
