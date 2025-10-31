package com.example.text2text

import com.example.text2text.data.Language
import com.example.text2text.service.TranslationService
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TranslationService
 * Note: These are basic structure tests. Full ML Kit testing requires instrumented tests.
 */
class TranslationServiceTest {

    @Test
    fun testLanguageEnumValues() {
        val languages = Language.values()
        assertEquals(3, languages.size)
        assertTrue(languages.contains(Language.SPANISH))
        assertTrue(languages.contains(Language.FRENCH))
        assertTrue(languages.contains(Language.CHINESE))
    }

    @Test
    fun testLanguageDisplayNames() {
        assertEquals("Spanish", Language.SPANISH.displayName)
        assertEquals("French", Language.FRENCH.displayName)
        assertEquals("Chinese", Language.CHINESE.displayName)
    }

    @Test
    fun testCacheStatsInitialization() {
        val service = TranslationService()
        val stats = service.getCacheStats()
        assertEquals(0, stats.first) // No entries initially
        assertEquals(0, stats.second) // No expired entries initially
    }
}