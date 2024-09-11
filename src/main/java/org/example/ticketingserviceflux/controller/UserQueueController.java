package org.example.ticketingserviceflux.controller;

import lombok.RequiredArgsConstructor;
import org.example.ticketingserviceflux.dto.AllowUserResponse;
import org.example.ticketingserviceflux.dto.AllowedUserResponse;
import org.example.ticketingserviceflux.dto.RankUserResponse;
import org.example.ticketingserviceflux.dto.RegisterUserResponse;
import org.example.ticketingserviceflux.service.UserQueueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-queue")
public class UserQueueController {

    private final UserQueueService userQueueService;

    @PostMapping
    public Mono<RegisterUserResponse> registerUser(@RequestParam long userId,
                                                    @RequestParam(name = "queueName", defaultValue = "default") String queueName) {
        return userQueueService.registerUserQueue(queueName,userId)
            .map(RegisterUserResponse::new);
    }

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(name = "count") Long count,
        @RequestParam(name = "queueName", defaultValue = "default") String queueName) {
        return userQueueService.allowUser(queueName,count)
            .map(allowed -> new AllowUserResponse(count, allowed));
    }

    @PostMapping("/allowed")
    public Mono<AllowedUserResponse> allowedUser(@RequestParam long userId,
        @RequestParam(name = "queueName", defaultValue = "default") String queueName) {
        return userQueueService.isAllowed(queueName,userId)
            .map(AllowedUserResponse::new);
    }

    @GetMapping("/rank")
    public Mono<RankUserResponse> getRank(@RequestParam long userId,
        @RequestParam(name = "queueName", defaultValue = "default") String queueName) {
        return userQueueService.getRank(queueName,userId)
            .map(RankUserResponse::new);
    }
}
