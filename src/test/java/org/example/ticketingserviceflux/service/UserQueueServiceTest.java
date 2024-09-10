package org.example.ticketingserviceflux.service;


import org.example.ticketingserviceflux.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
@ActiveProfiles("test")
class UserQueueServiceTest {

    @Autowired
    private UserQueueService userQueueService;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @BeforeEach
    public void beforeEach() {
        ReactiveRedisConnection redisConnection = reactiveRedisTemplate.getConnectionFactory().getReactiveConnection();
        redisConnection.serverCommands().flushAll().subscribe();
    }

    @Test
    void registerWaitQueue() {
        StepVerifier.create(userQueueService.registerUserQueue("default", 100L))
            .expectNext(1L)
            .verifyComplete();

        StepVerifier.create(userQueueService.registerUserQueue("default", 101L))
            .expectNext(2L)
            .verifyComplete();

        StepVerifier.create(userQueueService.registerUserQueue("default", 102L))
            .expectNext(3L)
            .verifyComplete();
    }

    @Test
    void alreadyRegisterWaitQueue() {
        StepVerifier.create(userQueueService.registerUserQueue("default", 100L))
            .expectNext(1L)
            .verifyComplete();

        StepVerifier.create(userQueueService.registerUserQueue("default", 100L))
            .expectError(ApiException.class)
            .verify();
    }

    @Test
    void emptyAllowUser() {
        StepVerifier.create(userQueueService.allowUser("default", 3L))
            .expectNext(0L)
            .verifyComplete();
    }

    @Test
    void allowUser() {
        StepVerifier.create(userQueueService.registerUserQueue("default", 100L)
                .then(userQueueService.registerUserQueue("default", 101L))
                .then(userQueueService.registerUserQueue("default", 102L))
                .then(userQueueService.allowUser("default", 2L)))
            .expectNext(2L)
            .verifyComplete();
    }

    @Test
    void allowUser2() {
        StepVerifier.create(userQueueService.registerUserQueue("default", 100L)
                .then(userQueueService.registerUserQueue("default", 101L))
                .then(userQueueService.registerUserQueue("default", 102L))
                .then(userQueueService.allowUser("default", 5L)))
            .expectNext(3L)
            .verifyComplete();
    }

    @Test
    void allowUserAfterRegisterWaitQueue() {
        StepVerifier.create(userQueueService.registerUserQueue("default", 100L)
                .then(userQueueService.registerUserQueue("default", 101L))
                .then(userQueueService.registerUserQueue("default", 102L))
                .then(userQueueService.allowUser("default", 3L))
                .then(userQueueService.registerUserQueue("default", 200L))
            )
            .expectNext(1L)
            .verifyComplete();
    }

    @Test
    void isNotAllowed() {
        StepVerifier.create(userQueueService.isAllowed("default", 100L))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void isNotAllowed2() {
        StepVerifier.create(userQueueService.registerUserQueue("default", 100L)
                .then(userQueueService.allowUser("default", 3L))
                .then(userQueueService.isAllowed("default", 101L)))
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    void isAllowed() {
        StepVerifier.create(userQueueService.registerUserQueue("default", 100L)
                .then(userQueueService.allowUser("default", 3L))
                .then(userQueueService.isAllowed("default", 100L)))
            .expectNext(true)
            .verifyComplete();
    }
}