package com.netoolhunter.app.data

import com.netoolhunter.app.domain.Repo

object DefaultRepos {
    val ALL: List<Repo> = listOf(
        Repo(
            id = "kali-rolling",
            name = "Kali Rolling",
            sourceLine = "deb http://http.kali.org/kali kali-rolling main contrib non-free non-free-firmware",
            enabled = true,
            isCustom = false
        ),
        Repo(
            id = "kali-nethunter",
            name = "Kali NetHunter",
            sourceLine = "deb https://http.kali.org/kali kali-rolling main non-free contrib",
            enabled = true,
            isCustom = false
        ),
        Repo(
            id = "offsec",
            name = "Offensive Security",
            sourceLine = "deb https://http.kali.org/kali kali-experimental main contrib non-free",
            enabled = false,
            isCustom = false
        ),
        Repo(
            id = "blackarch",
            name = "BlackArch (sources)",
            sourceLine = "# BlackArch — solo notas, no se aplica a apt directamente",
            enabled = false,
            isCustom = false
        )
    )

    val byId: Map<String, Repo> = ALL.associateBy { it.id }
}
