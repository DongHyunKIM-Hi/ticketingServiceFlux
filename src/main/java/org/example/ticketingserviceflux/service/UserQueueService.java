package org.example.ticketingserviceflux.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.example.ticketingserviceflux.controller.exception.ApiException;
import org.example.ticketingserviceflux.controller.exception.ErrorCode;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public final String USER_QUEUE_WAIT_KEY = "user-queue:%s";

    public Mono<Long> registerUserQueue(final String queueName,final Long userId) {
        var unixTime = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queueName), userId.toString(), unixTime)
            .filter(it -> it)
            .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTER_USER_ID.build()))
            .flatMap(it -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queueName), userId.toString()))
            .map(it -> it >= 0 ? it +1 : it);
    }
}
