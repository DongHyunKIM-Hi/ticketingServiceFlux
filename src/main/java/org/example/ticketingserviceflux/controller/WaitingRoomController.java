package org.example.ticketingserviceflux.controller;

import lombok.RequiredArgsConstructor;
import org.example.ticketingserviceflux.service.UserQueueService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class WaitingRoomController {

    private final UserQueueService userQueueService;

    // 잠깐 대기하는 대기 큐
    // 순서가 오면 원하는 url로 접속 할 수 있음
    // ex) http://localhost:9010/user-queue/rank?userId=100&redirect_url=www.naver.com
    @GetMapping("/waiting-room")
    Mono<Rendering> waitingRoomPage(@RequestParam long userId,
                                    @RequestParam(name = "queueName", defaultValue = "default") String queueName,
                                    @RequestParam(name = "redirect_url") String redirectUrl
    ) {


        return userQueueService.isAllowed(queueName, userId)
            .filter(allowed -> allowed)
            .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
            .switchIfEmpty(
                userQueueService.registerUserQueue(queueName, userId)
                    .onErrorResume(ex -> userQueueService.getRank(queueName,userId))
                    .map(rank -> Rendering.view("waiting-room.html")
                        .modelAttribute("number", rank)
                        .modelAttribute("userId", userId)
                        .modelAttribute("queue", queueName)
                        .build()
            ));
    }
}
