package Project.Pocket.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){

        return new LettuceConnectionFactory(host,port);
    }
    //RedisConnectionFactory는 Redis 서버와의 연결을 관리하는 팩토리입니다.
    // LettuceConnectionFactory는 Lettuce 클라이언트를 사용하여 Redis 연결을 설정합니다.

    /*
            RedisTemplate: Redis data access code를 간소화 하기 위해 제공되는 클래스이다.
                           주어진 객체들을 자동으로 직렬화/역직렬화 하며 binary 데이터를 Redis에 저장한다.
                           기본설정은 JdkSerializationRedisSerializer 이다.

            StringRedisSerializer: binary 데이터로 저장되기 때문에 이를 String 으로 변환시켜주며(반대로도 가능) UTF-8 인코딩 방식을 사용한다.
            GenericJackson2JsonRedisSerializer는 Redis에 저장되는 객체를 JSON 형식으로 직렬화하고, 역직렬화할 수 있도록 합니다.
         */
    @Bean
    public RedisTemplate<String,String> redisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        //redisConnectionFactory() 메서드를 사용하여 RedisConnectionFactory를 가져와서 RedisTemplate에 설정합니다.
        return redisTemplate;
    }

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
