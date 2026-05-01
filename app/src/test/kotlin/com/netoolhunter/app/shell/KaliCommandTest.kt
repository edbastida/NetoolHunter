package com.netoolhunter.app.shell

import com.netoolhunter.app.domain.Category
import com.netoolhunter.app.domain.Installer
import com.netoolhunter.app.domain.Tool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KaliCommandTest {

    private fun tool(installer: Installer, id: String = "tool", name: String = "Tool") = Tool(
        id = id, name = name, description = "", category = Category.RECON,
        emoji = "🔧", installer = installer
    )

    @Test
    fun `apt install uses apt-get install -y`() {
        val cmd = KaliCommand.install(Installer.Apt("nmap"))
        assertEquals("apt-get install -y nmap", cmd)
    }

    @Test
    fun `go install uses go install module`() {
        val cmd = KaliCommand.install(Installer.Go("github.com/x/y@latest"))
        assertEquals("go install github.com/x/y@latest", cmd)
    }

    @Test
    fun `pip install includes break-system-packages`() {
        val cmd = KaliCommand.install(Installer.Pip("holehe"))
        assertTrue(cmd.contains("--break-system-packages"))
        assertTrue(cmd.startsWith("pip install holehe"))
    }

    @Test
    fun `pipx install uses pipx install source`() {
        val cmd = KaliCommand.install(Installer.Pipx("git+https://x/y"))
        assertEquals("pipx install git+https://x/y", cmd)
    }

    @Test
    fun `git install with cloneTo uses provided target`() {
        val cmd = KaliCommand.install(Installer.Git("https://github.com/a/b", cloneTo = "/opt/b"))
        assertTrue(cmd.contains("git clone https://github.com/a/b /opt/b"))
    }

    @Test
    fun `git install with postInstall chains via cd target && cmd`() {
        val cmd = KaliCommand.install(
            Installer.Git(
                "https://github.com/a/b",
                cloneTo = "/opt/b",
                postInstall = listOf("pip install -r requirements.txt")
            )
        )
        assertTrue(cmd.contains("git clone https://github.com/a/b /opt/b"))
        assertTrue(cmd.contains("cd /opt/b && pip install -r requirements.txt"))
        assertTrue(cmd.contains(" && "))
    }

    @Test
    fun `docker install uses docker pull`() {
        val cmd = KaliCommand.install(Installer.Docker("opensecurity/x", "-it -p 8000:8000"))
        assertEquals("docker pull opensecurity/x", cmd)
    }

    @Test
    fun `script install pipes curl into bash`() {
        val cmd = KaliCommand.install(Installer.Script("https://x/install"))
        assertEquals("curl -sSL https://x/install | bash", cmd)
    }

    @Test
    fun `apt isInstalled uses dpkg grep ii`() {
        val cmd = KaliCommand.isInstalled(tool(Installer.Apt("nmap")))
        assertTrue(cmd.contains("dpkg -l nmap"))
        assertTrue(cmd.contains("'^ii'"))
    }

    @Test
    fun `pip uninstall includes break-system-packages`() {
        val cmd = KaliCommand.uninstall(tool(Installer.Pip("foo")))
        assertTrue(cmd.contains("--break-system-packages"))
        assertTrue(cmd.startsWith("pip uninstall -y foo"))
    }

    @Test
    fun `git isInstalled checks cloneTo directory exists`() {
        val cmd = KaliCommand.isInstalled(tool(Installer.Git("https://x/y", cloneTo = "/opt/y")))
        assertEquals("test -d /opt/y", cmd)
    }
}
