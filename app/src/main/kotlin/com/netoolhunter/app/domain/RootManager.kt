package com.netoolhunter.app.domain

/**
 * Root provider detected on the device. Drives the per-manager guidance text
 * shown when the app's su context can't see the NetHunter chroot binaries —
 * each manager has its own UI for granting "global mount + chroot capability".
 */
enum class RootManager {
    MagiskOfficial,
    KernelSU,
    KernelSUNext,
    APatch,
    Unknown
}
