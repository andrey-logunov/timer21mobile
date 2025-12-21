package com.logunov.timer21mobile

object Motivator {
    private val phrases = listOf(
        "Come on! 🔥",
        "Let's go! 🚀",
        "You got this! 💪",
        "Keep going! ⚡",
        "Go for it! 🎯",
        "Push it! 💥",
        "You've got it! ⭐",
        "Almost there! 🏁",
        "Don't stop now! ⏩",
        "Bring it on! 🦁",
        "One more! 💯",
        "Keep it up! 📈",
        "You're killing it! 😎",
        "Come on, champ! 🏆",
        "Let's do this! 👊",
        "Crush it! 🗡️",
        "Finish strong! 🏋️",
        "Dig deep! ⛏️",
        "Make it happen! ✨",
        "You're unstoppable! 🌟",
        "Go get 'em! 🐯",
        "Now or never! ⏰"
    )

    fun getRandom(): String = phrases.random()
    fun getRandomPlain(): String = phrases.random().replace(Regex("[\\p{So}\\p{Sc}\\p{Sk}]"), "")
}