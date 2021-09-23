package org.example.wordladder.words;

import org.example.wordladder.exceptions.NoResourceForDictionaryException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DictionaryTests {
    private static final int[] VALID_DICTIONARY_LENGTHS = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final Map<Integer, Integer> EXPECTED_DICTIONARY_SIZES = new HashMap<>() {{
        put(2, 127);
        put(3, 1347);
        put(4, 5638);
        put(5, 12972);
        put(6, 23033);
        put(7, 34342);
        put(8, 42150);
        put(9, 42933);
        put(10, 37235);
        put(11, 29027);
        put(12, 21025);
        put(13, 14345);
        put(14, 9397);
        put(15, 5925);
    }};

    @Test
    void canLoadDictionaries() {
        for (int wordLength: VALID_DICTIONARY_LENGTHS) {
            Dictionary dictionary = new Dictionary(wordLength);
            assertNotNull(dictionary);
            assertFalse(dictionary.isEmpty());
            assertEquals(EXPECTED_DICTIONARY_SIZES.get(wordLength), (Integer)dictionary.size());
        }
    }

    @Test
    void invalidLengthDictionaryFailsToLoad() {
        assertThrows(NoResourceForDictionaryException.class,
                () -> new Dictionary(VALID_DICTIONARY_LENGTHS[0] - 1));
    }

    @Test
    void invalidLengthDictionaryFailsToLoad2() {
        assertThrows(NoResourceForDictionaryException.class,
                () -> new Dictionary(VALID_DICTIONARY_LENGTHS[VALID_DICTIONARY_LENGTHS.length - 1] + 1));
    }

    @Test
    void canLoadDictionariesFromFactory() {
        for (int wordLength: VALID_DICTIONARY_LENGTHS) {
            Dictionary dictionary = Dictionary.Factory.forWordLength(wordLength);
            assertNotNull(dictionary);
            assertFalse(dictionary.isEmpty());
        }
    }

    @Test
    void dictionaryWordHasVariants() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("cat");
        assertNotNull(word);

        List<Word> wordVariants = word.getLinkedWords();
        assertFalse(wordVariants.isEmpty());
        // and check the word itself is not in its list of variants...
        assertFalse(wordVariants.contains(word));
    }

    @Test
    void dictionaryWordIsIslandWord() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("IWI");
        assertNotNull(word);

        List<Word> wordVariants = word.getLinkedWords();
        assertTrue(wordVariants.isEmpty());
        assertTrue(word.isIslandWord());
    }

    @Test
    void differencesBetweenLinkedWords() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("cat");
        assertNotNull(word);
        assertFalse(word.getLinkedWords().isEmpty());
        for (Word linkedWord: word.getLinkedWords()) {
            assertEquals(1, word.differences(linkedWord));
        }
    }

    @Test
    void wordsAreInterLinked() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("cat");
        assertNotNull(word);
        assertFalse(word.getLinkedWords().isEmpty());
        for (Word linkedWord: word.getLinkedWords()) {
            assertTrue(linkedWord.getLinkedWords().contains(word));
        }
    }
}
