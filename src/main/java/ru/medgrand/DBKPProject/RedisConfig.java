package ru.medgrand.DBKPProject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.medgrand.DBKPProject.Pub_Sub.OrderEventsSubscriber;

import java.time.Duration;

@Configuration
//@EnableRedisHttpSession
public class RedisConfig {

    public static final String NEW_ORDER_TOPIC = "orders:new";
    public static final String ORDER_STATUS_UPDATE_TOPIC = "orders:status_update";

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter newOrderListenerAdapter,
            MessageListenerAdapter orderStatusUpdateListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(newOrderListenerAdapter, new ChannelTopic(NEW_ORDER_TOPIC));
        container.addMessageListener(orderStatusUpdateListenerAdapter, new ChannelTopic(ORDER_STATUS_UPDATE_TOPIC));
        return container;
    }

    @Bean
    public MessageListenerAdapter newOrderListenerAdapter(OrderEventsSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleNewOrderMessage"); // Указываем метод
    }

    @Bean
    public MessageListenerAdapter orderStatusUpdateListenerAdapter(OrderEventsSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "handleOrderStatusUpdateMessage"); // Указываем метод
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        return objectMapper;
    }

    @Bean
    public GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer(ObjectMapper objectMapper) {
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer) {
        return (builder) -> builder
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig() // Берет настройки из properties
                                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
                );
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer) {
        return jackson2JsonRedisSerializer;
    }

}
