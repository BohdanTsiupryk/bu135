package bts.bu135.service

import bts.bu135.config.AppConfigData
import bts.bu135.config.GitData
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportConfigCallback
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.*
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig
import org.eclipse.jgit.util.FS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Service
class GitService(
        val appData: AppConfigData,
        val gitData: GitData,
) {

    private val logger: Logger = LoggerFactory.getLogger(GitService::class.java)

    fun backup() {

        logger.info("Start backup processing")
        logger.info("Folders list: \n${gitData.folders.stream().map { "${it.pref} - ${it.path}" }.toList()}")

        gitData.folders.stream().forEach {

            if (!checkFolder(it.path)) {
                logger.error("${it.pref} - root or .git don't exist")
                return@forEach
            }

            val git = git(it.path)
            val createSshSessionFactory = createSshSessionFactory(appData.ssh.file, appData.ssh.passphrase)

            val transportConfig = object : TransportConfigCallback {
                override fun configure(transport: Transport?) {
                    if (transport is SshTransport) {
                        transport.sshSessionFactory = createSshSessionFactory
                    }
                }
            }

            try {
                val generateCommitMessage = generateCommitMessage(it.pref)

                git.pull()
                        .setTransportConfigCallback(transportConfig)
                        .call()
                git.add().addFilepattern(".").call()
                git.commit().setMessage(generateCommitMessage).call()
                git.push()
                        .setTransportConfigCallback(transportConfig)
                        .setRemote(it.remote)
                        .call()

                logger.info("${it.pref} - backup success, commit message '$generateCommitMessage'")

            }catch (e: Exception) {
                logger.error("${it.pref} - ${e.message!!}", )
            }
        }

        logger.info("Finish backup processing")
    }

    private fun generateCommitMessage(pref: String?): String {
        return "$pref - ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))}"
    }

    private fun git(it: String?): Git {
        val existingRepo: Repository = FileRepositoryBuilder()
                .setGitDir(File("$it/.git"))
                .build()
        val git = Git(existingRepo)
        return git
    }

    private fun checkFolder(path: String) : Boolean {
        val file = File(path)

        return file.isDirectory &&
                file.list()?.contains(".git") == true
    }

    private fun createSshSessionFactory(sshKeyFile: String, sshPassphrase: String?): SshSessionFactory {
        val sshSessionFactory = object : JschConfigSessionFactory() {
            override fun configure(hc: OpenSshConfig.Host?, session: Session?) {
                // Configure session here if necessary, e.g.:
                 session?.setConfig("StrictHostKeyChecking", "no")
            }

            override fun createDefaultJSch(fs: FS?): JSch {
                val jsch = super.createDefaultJSch(fs)
                // Load SSH key
                jsch.addIdentity(sshKeyFile, sshPassphrase)
                return jsch
            }
        }

        return sshSessionFactory
    }
}
