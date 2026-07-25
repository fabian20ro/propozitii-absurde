package scrabble.phrases.decorators

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import scrabble.phrases.providers.ISentenceProvider

class DexlineLinkAdderTest {

    class MockSentenceProvider(private val sentence: String) : ISentenceProvider {
        override fun getSentence(): String = sentence
    }

    @Test
    fun `should wrap word in dexonline link`() {
        val provider = MockSentenceProvider("test")
        val decorator = DexlineLinkAdder(provider)
        val result = decorator.getSentence()

        val expected = "<a href=\"https://dexonline.ro/definitie/test\" target=\"_blank\" rel=\"noopener\" data-word=\"test\">test</a>"
        assertEquals(expected, result)
    }

    @Test
    fun `should handle multiple words and punctuation`() {
        val provider = MockSentenceProvider("hello, world!")
        val decorator = DexlineLinkAdder(provider)
        val result = decorator.getSentence()

        val expected = "<a href=\"https://dexonline.ro/definitie/hello\" target=\"_blank\" rel=\"noopener\" data-word=\"hello\">hello</a>, <a href=\"https://dexonline.ro/definitie/world\" target=\"_blank\" rel=\"noopener\" data-word=\"world\">world</a>!"
        assertEquals(expected, result)
    }

    @Test
    fun `should handle apostrophes`() {
        val provider = MockSentenceProvider("l'amare")
        val decorator = DexlineLinkAdder(provider)
        val result = decorator.getSentence()

        // The regex [\p{L}']+ matches l'amare as one token
        val expected = "<a href=\"https://dexonline.ro/definitie/l%27amare\" target=\"_blank\" rel=\"noopener\" data-word=\"l%27amare\">l&#39;amare</a>"
        assertEquals(expected, result)
    }

    @Test
    fun `should handle multiple apostrophes in one word`() {
        val provider = MockSentenceProvider("l'amare's")
        val decorator = DexlineLinkAdder(provider)
        val result = decorator.getSentence()

        // Multiple apostrophes: all escaped to &#39;, URL-encoded as %27
        val expected = "<a href=\"https://dexonline.ro/definitie/l%27amare%27s\" target=\"_blank\" rel=\"noopener\" data-word=\"l%27amare%27s\">l&#39;amare&#39;s</a>"
        assertEquals(expected, result)
    }

    @Test
    fun `should use dexonline base URL in href`() {
        val provider = MockSentenceProvider("test")
        val decorator = DexlineLinkAdder(provider)
        val result = decorator.getSentence()

        assertTrue(
            result.contains("\"https://dexonline.ro/definitie/test\""),
            "href must use the exact dexonline base URL so frontend sanitizer accepts it; got: $result"
        )
    }

    @Test
    fun `should leave non-letter input unchanged`() {
        val provider = MockSentenceProvider("123!")
        val decorator = DexlineLinkAdder(provider)
        val result = decorator.getSentence()

        assertEquals("123!", result, "input with no letters must pass through without generating links")
    }
}
