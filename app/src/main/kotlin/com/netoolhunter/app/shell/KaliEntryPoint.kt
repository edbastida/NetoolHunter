package com.netoolhunter.app.shell

/**
 * Resolves the wrapper used to execute commands inside the Kali NetHunter
 * chroot. Tries, in order:
 *
 *   1. `nh -r 'cmd'`        — provided by NetHunter Terminal (com.offsec.nhterm)
 *   2. stdin pipe to        — provided by the NetHunter Magisk / KernelSU /
 *      `bootkali_bash`        KernelSU Next / APatch module
 *
 * Notes on (2): `bootkali_bash` ignores command-line args and always launches
 * an interactive login bash inside the chroot. We feed our payload via stdin
 * with BEGIN/END sentinels, then filter out bootkali's status banners with
 * awk so callers only receive the user command's output. The exit code of
 * the inner command is propagated through the END sentinel.
 *
 * On many devices (e.g. KernelSU / KernelSU Next) the raw `chroot` syscall
 * is denied to a vanilla `su -c` shell ("Operation not permitted") even when
 * SELinux is Permissive. Going through `bootkali_bash` works because
 * NetHunter's installer adds the right binary capabilities. That is why we
 * never call `chroot` directly here.
 *
 * Adding a new install layout = appending a path to one of the lists below.
 */
object KaliEntryPoint {

    private const val SB = "___NTH_BEGIN___"
    private const val SE = "___NTH_END___"

    private val NH_CANDIDATES = listOf(
        "/data/data/com.offsec.nhterm/files/usr/bin/nh",
        "/system/bin/nh",
        "/system/xbin/nh",
        "/sbin/nh",
        "/data/adb/modules/nethunter/system/bin/nh"
    )

    private val BOOTKALI_BASH_CANDIDATES = listOf(
        "/system/bin/bootkali_bash",
        "/data/adb/modules/nethunter/system/bin/bootkali_bash",
        "/data/data/com.offsec.nethunter/scripts/bootkali_bash",
        "/data/user/0/com.offsec.nethunter/scripts/bootkali_bash"
    )

    private const val CHROOT_DIR = "/data/local/nhsystem/kali-arm64"
    private const val CHROOT_BIN = "/system/bin/chroot"

    private val PICK_SNIPPET: String = buildString {
        append("EXEC=; MODE=; TRACE=; ")
        for (p in NH_CANDIDATES) {
            append("if [ -e $p ]; then T=e; else T=.; fi; ")
            append("if [ -x $p ]; then T=\${T}x; fi; ")
            append("TRACE=\"\$TRACE $p:\$T\"; ")
            append("if [ -z \"\$EXEC\" ] && [ -x $p ]; then EXEC=$p; MODE=nh; fi; ")
        }
        append("if [ -z \"\$EXEC\" ] && command -v nh >/dev/null 2>&1; then EXEC=\$(command -v nh); MODE=nh; TRACE=\"\$TRACE PATH-nh:\$EXEC\"; fi; ")
        for (p in BOOTKALI_BASH_CANDIDATES) {
            append("if [ -e $p ]; then T=e; else T=.; fi; ")
            append("if [ -x $p ]; then T=\${T}x; fi; ")
            append("TRACE=\"\$TRACE $p:\$T\"; ")
            append("if [ -z \"\$EXEC\" ] && [ -x $p ]; then EXEC=$p; MODE=bootkali; fi; ")
        }
        // Last resort: raw chroot. Works if (a) /system/bin/chroot is visible,
        // (b) the chroot dir exists, (c) the calling context has CAP_SYS_CHROOT
        // (granted by KSU "nethunter.root" template or Magisk equivalent).
        append("if [ -e $CHROOT_BIN ]; then T=e; else T=.; fi; ")
        append("if [ -x $CHROOT_BIN ]; then T=\${T}x; fi; ")
        append("TRACE=\"\$TRACE $CHROOT_BIN:\$T\"; ")
        append("if [ -z \"\$EXEC\" ] && [ -x $CHROOT_BIN ] && [ -d $CHROOT_DIR ]; then EXEC=$CHROOT_BIN; MODE=raw; fi; ")
        append("if [ -z \"\$EXEC\" ]; then ")
        append("echo \"NetoolHunter: no kali entrypoint.\" >&2; ")
        append("echo \"  id=\$(id 2>&1)\" >&2; ")
        append("echo \"  trace=\$TRACE\" >&2; ")
        append("echo \"  ls_data_adb=\$(ls -la /data/adb 2>&1 | head -3 | tr '\\n' '|')\" >&2; ")
        append("echo \"  ls_data_data=\$(ls /data/data 2>&1 | head -5 | tr '\\n' ' ')\" >&2; ")
        append("echo \"  ls_nhterm=\$(ls /data/data/com.offsec.nhterm/files 2>&1 | head -5 | tr '\\n' ' ')\" >&2; ")
        append("echo \"  ls_nethunter=\$(ls /data/data/com.offsec.nethunter/scripts 2>&1 | head -5 | tr '\\n' ' ')\" >&2; ")
        append("echo \"  ls_modules=\$(ls /data/adb/modules 2>&1 | head -5 | tr '\\n' ' ')\" >&2; ")
        append("echo \"  mount_count=\$(wc -l < /proc/self/mountinfo 2>/dev/null)\" >&2; ")
        append("echo \"  mount_nethunter=\$(grep -c nethunter /proc/self/mountinfo 2>/dev/null)\" >&2; ")
        append("echo \"  mount_modules=\$(grep -c '/data/adb/modules' /proc/self/mountinfo 2>/dev/null)\" >&2; ")
        append("exit 127; fi; ")
    }

    private val AWK_FILTER: String =
        "awk '" +
            "/^${SB}${'$'}/{f=1;next} " +
            "/^${SE}:[0-9]+${'$'}/{sub(/^${SE}:/,\"\");rc=${'$'}0+0;seen=1;exit rc} " +
            "f{print} " +
            "END{if(!seen)exit 99}" +
            "'"

    /** Wraps [command] so it runs inside the Kali chroot. Single-quote-safe. */
    fun wrap(command: String): String {
        val esc = command.replace("'", "'\\''")
        val innerScript = "echo $SB; ( $esc ); echo $SE:\$?; exit"
        val innerEsc = innerScript.replace("'", "'\\''")
        val nhInvoke = "\"\$EXEC\" -r '$esc'"
        val pipeInvoke = "printf '%s\\n' '$innerEsc' | \"\$EXEC\" 2>&1 | $AWK_FILTER"
        // raw chroot mode: launch the chroot's /bin/bash directly with our cmd.
        // Mounts (proc, sys, dev, devpts) are expected to be set up already by
        // NetHunter Chroot Manager — we don't redo them.
        val rawInvoke = "unset LD_PRELOAD; \"\$EXEC\" $CHROOT_DIR /usr/bin/env -i " +
            "HOME=/root USER=root SHELL=/bin/bash TERM=xterm-256color LANG=C.UTF-8 " +
            "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin " +
            "/bin/bash -c '$esc'"
        return PICK_SNIPPET +
            "if [ \"\$MODE\" = nh ]; then $nhInvoke; " +
            "elif [ \"\$MODE\" = bootkali ]; then $pipeInvoke; " +
            "else $rawInvoke; fi"
    }

    /** Probe: prints `ok` iff a working entrypoint exists and the chroot responds. */
    val probeCommand: String = wrap("echo ok")
}
