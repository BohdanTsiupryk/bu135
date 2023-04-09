package bts.bu135.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "settings")
data class AppConfigData(
        var schedule: String,
        var ssh: SshData,
)

@ConfigurationProperties(prefix = "git")
data class GitData(
        var folders: List<Folders> = ArrayList()
)

data class SshData(
        var file: String,
        var passphrase: String,
)

data class Folders(
        var path: String,
        var pref: String,
        var remote: String?,
)