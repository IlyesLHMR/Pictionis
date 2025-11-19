package com.pictionis.ap.utils

object WordsLibrary {
    private val words = listOf(
        // Animaux
        "chat", "chien", "oiseau", "poisson", "lapin", "éléphant", "lion", "tigre", "ours", "souris",

        // Fruits
        "pomme", "banane", "orange", "fraise", "raisin", "cerise", "poire", "ananas", "pastèque", "citron",

        // Objets quotidiens
        "table", "chaise", "stylo", "livre", "téléphone", "voiture", "vélo", "maison", "arbre", "soleil",

        // Métiers
        "docteur", "professeur", "pompier", "policier", "cuisinier", "musicien", "peintre", "danseur",

        // Sports
        "football", "basketball", "tennis", "natation", "ski", "boxe", "course", "yoga",

        // Nature
        "montagne", "océan", "rivière", "forêt", "fleur", "nuage", "pluie", "neige", "vent",

        // Nourriture
        "pizza", "burger", "pain", "fromage", "chocolat", "gâteau", "salade", "soupe", "café", "thé"
    )

    fun getRandomWord(): String {
        return words.random()
    }

    fun getAllWords(): List<String> {
        return words
    }
}

