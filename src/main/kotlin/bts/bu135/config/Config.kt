package bts.bu135.config

import bts.bu135.service.GitService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled


@Configuration
@EnableConfigurationProperties(AppConfigData::class, GitData::class)
@EnableScheduling
class Config(
        val gitService: GitService,
) {

    @Scheduled(cron = "\${settings.schedule}")
    fun backupSchedule() = gitService.backup()
}