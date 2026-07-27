package scrabble.phrases.providers

import org.jboss.logging.Logger
import scrabble.phrases.repository.WordRepository

class ComparisonProvider(
    private val repo: WordRepository,
    private val minRarity: Int = 1,
    private val maxRarity: Int = WordRepository.DEFAULT_MAX_RARITY
) : ISentenceProvider {

    companion object {
        @JvmField val log: Logger = Logger.getLogger(ComparisonProvider::class.java)
    }

    override fun getSentence(): String {
        val noun1 = repo.getRandomNoun(minRarity = minRarity, maxRarity = maxRarity)
        log.debugf("Comparison selected noun='%s' (gender=%s) for rarity %d..%d", noun1.word, noun1.gender, minRarity, maxRarity)
        val adj = repo.getRandomAdjective(minRarity = minRarity, maxRarity = maxRarity)
        val noun2 = repo.getRandomNoun(minRarity = minRarity, maxRarity = maxRarity, exclude = setOf(noun1.word))

        return "${noun1.articulated} e mai ${adj.forGender(noun1.gender)} decât ${noun2.articulated}."
    }
}
