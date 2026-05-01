package com.netoolhunter.app.shell

import com.netoolhunter.app.domain.Installer
import com.netoolhunter.app.domain.Tool

/**
 * Builds the chroot-side shell command for each Installer variant.
 * Never emits inline strings — always go through here so we have a single
 * place to audit destructive commands.
 */
object KaliCommand {
    fun install(installer: Installer): String = when (installer) {
        is Installer.Apt    -> "apt-get install -y ${installer.pkg}"
        is Installer.Go     -> "go install ${installer.module}"
        is Installer.Pip    -> "pip install ${installer.pkg} --break-system-packages"
        is Installer.Pipx   -> "pipx install ${installer.source}"
        is Installer.Git    -> buildGitCommand(installer)
        is Installer.Docker -> "docker pull ${installer.image}"
        is Installer.Script -> "curl -sSL ${installer.curlUrl} | bash"
    }

    fun uninstall(tool: Tool): String = when (val i = tool.installer) {
        is Installer.Apt    -> "apt-get remove -y ${i.pkg}"
        is Installer.Go     -> "rm -f \$(go env GOPATH)/bin/${tool.id}"
        is Installer.Pip    -> "pip uninstall -y ${i.pkg} --break-system-packages"
        is Installer.Pipx   -> "pipx uninstall ${tool.id}"
        is Installer.Git    -> "rm -rf ${i.cloneTo ?: defaultGitTarget(i.repo)}"
        is Installer.Docker -> "docker rmi ${i.image}"
        is Installer.Script -> "echo 'Desinstalación manual requerida para ${tool.name}'"
    }

    fun isInstalled(tool: Tool): String = when (val i = tool.installer) {
        is Installer.Apt    -> "dpkg -l ${i.pkg} 2>/dev/null | grep -q '^ii'"
        is Installer.Go     -> "test -x \$(go env GOPATH)/bin/${tool.id}"
        is Installer.Pip    -> "pip show ${i.pkg} >/dev/null 2>&1"
        is Installer.Pipx   -> "pipx list 2>/dev/null | grep -qi ${tool.id}"
        is Installer.Git    -> "test -d ${i.cloneTo ?: defaultGitTarget(i.repo)}"
        is Installer.Docker -> "docker images -q ${i.image} | grep -q ."
        is Installer.Script -> "command -v ${tool.id} >/dev/null 2>&1"
    }

    private fun buildGitCommand(g: Installer.Git): String {
        val target = g.cloneTo ?: "/opt/$(basename ${g.repo} .git)"
        val clone = "git clone ${g.repo} $target"
        val post = g.postInstall.joinToString(" && ") { "cd $target && $it" }
        return if (post.isBlank()) clone else "$clone && $post"
    }

    private fun defaultGitTarget(repo: String): String =
        "/opt/" + repo.substringAfterLast('/').removeSuffix(".git").lowercase()
}
