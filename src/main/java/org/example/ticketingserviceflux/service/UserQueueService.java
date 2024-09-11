package org.example.ticketingserviceflux.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ticketingserviceflux.exception.ErrorCode;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueueService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    public final String USER_QUEUE_WAIT_KEY = "user-queue:%s:wait";
    public final String USER_QUEUE_PROCEED_KEY = "user-queue:%s:proceed";
    private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "users-queue:*:wait";


    public Mono<Long> registerUserQueue(final String queueName,final Long userId) {
        var unixTime = Instant.now().getEpochSecond();
        return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queueName), userId.toString(), unixTime)
            .filter(it -> it)
            .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTER_USER_ID.build()))
            .flatMap(it -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queueName), userId.toString()))
            .map(it -> it >= 0 ? it +1 : it);
    }


    public Mono<Long> allowUser(final String queueName, final long count) {
        return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queueName), count)
            .flatMap(user -> reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY.formatted(queueName), user.getValue(), Instant.now().getEpochSecond()))
            .count();
    }

    public Mono<Boolean> isAllowed(final String queueName, final Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY.formatted(queueName), userId.toString())
            .defaultIfEmpty(-1L)
            .map(rank -> rank >=0 )
            ;
    }

    public Mono<Long> getRank(final String queueName, final Long userId) {
        return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queueName),userId.toString())
            .defaultIfEmpty(-1L)
            .map(rank -> rank + 1);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 3000)
    public void scheduleAllowedUser() {
        log.info("Schedule is processing!");

        var maxUserCount = 3;

        reactiveRedisTemplate.scan(ScanOptions.scanOptions()
                .match(USER_QUEUE_WAIT_KEY_FOR_SCAN)
                .count(100)
                .build())
            .map(key -> key.split(":")[1])
            .flatMap(queue -> allowUser(queue,maxUserCount).map(allowed -> Tuples.of(queue,allowed)))
            .doOnNext(tuple -> log.info("Tried %d and allowed %d members of %s queue".formatted(maxUserCount,tuple.getT2(),tuple.getT1())))
            .subscribe();
    }
}
