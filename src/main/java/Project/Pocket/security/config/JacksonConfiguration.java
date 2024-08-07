package Project.Pocket.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new ObjectMapper()
                // 객체가 비어있을 때 에러 발생하지 않도록 설정
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                // 순환 참조가 발생해도 에러 발생하지 않도록 설정
                .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
                // 타입 식별자 없이 객체를 직렬화하도록 설정
                .configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
    }
}
