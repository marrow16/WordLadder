package org.example.wordladder.words;

import org.example.wordladder.exceptions.NoResourceForDictionaryException;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class DictionaryTests {
    private static final int[] VALID_DICTIONARY_LENGTHS = new int[] {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    @Test
    public void canLoadDictionaries() {
        for (int wordLength: VALID_DICTIONARY_LENGTHS) {
            Dictionary dictionary = new Dictionary(wordLength);
            assertNotNull(dictionary);
            assertFalse(dictionary.isEmpty());
        }
    }

    @Test(expected = NoResourceForDictionaryException.class)
    public void invalidLengthDictionaryFailsToLoad() {
        new Dictionary(VALID_DICTIONARY_LENGTHS[0] - 1);
    }

    @Test(expected = NoResourceForDictionaryException.class)
    public void invalidLengthDictionaryFailsToLoad2() {
        new Dictionary(VALID_DICTIONARY_LENGTHS[VALID_DICTIONARY_LENGTHS.length - 1] + 1);
    }

    @Test
    public void canLoadDictionariesFromFactory() {
        for (int wordLength: VALID_DICTIONARY_LENGTHS) {
            Dictionary dictionary = Dictionary.Factory.forWordLength(wordLength);
            assertNotNull(dictionary);
            assertFalse(dictionary.isEmpty());
        }
    }

    @Test
    public void dictionaryWordHasVariants() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("cat");
        assertNotNull(word);

        List<Word> wordVariants = word.getLinkedWords();
        assertFalse(wordVariants.isEmpty());
        // and check the word itself is not in its list of variants...
        assertFalse(wordVariants.contains(word));
    }

    @Test
    public void dictionaryWordIsIslandWord() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("IWI");
        assertNotNull(word);

        List<Word> wordVariants = word.getLinkedWords();
        assertTrue(wordVariants.isEmpty());
        assertTrue(word.isIslandWord());
    }

    @Test
    public void differencesBetweenLinkedWords() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("cat");
        assertNotNull(word);
        assertFalse(word.getLinkedWords().isEmpty());
        for (Word linkedWord: word.getLinkedWords()) {
            assertEquals(1, word.differences(linkedWord));
        }
    }

    @Test
    public void wordsAreInterLinked() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("cat");
        assertNotNull(word);
        assertFalse(word.getLinkedWords().isEmpty());
        for (Word linkedWord: word.getLinkedWords()) {
            assertTrue(linkedWord.getLinkedWords().contains(word));
        }
    }
}
