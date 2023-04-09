package bts.bu135.controller

import bts.bu135.config.AppConfigData
import bts.bu135.service.GitService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class GitController(val service: GitService) {

    @GetMapping("/folders")
    fun backupAll(): Mono<String> {

        service.backup()

        return Mono.just("200")
    }
}