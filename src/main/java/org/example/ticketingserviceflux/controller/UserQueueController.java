package org.example.ticketingserviceflux.controller;

import lombok.RequiredArgsConstructor;
import org.example.ticketingserviceflux.dto.RegisterUserResponse;
import org.example.ticketingserviceflux.service.UserQueueService;
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

}
