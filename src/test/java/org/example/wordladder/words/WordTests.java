package org.example.wordladder.words;

import org.example.wordladder.exceptions.BadWordException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordTests {
    @Test
    void canCreateWord() {
        Word word = new Word("cat");
        assertEquals("CAT", word.toString());
    }

    @Test
    void failsToCreateWordWithReservedChar() {
        assertThrows(BadWordException.class,
                () -> new Word("c_t"));
    }

    @Test
    void variationPatternsAreCorrect() {
        Word word = new Word("cat");
        List<String> variants = word.getVariationPatterns();
        assertEquals(3, variants.size());
        assertEquals("_AT", variants.get(0));
        assertEquals("C_T", variants.get(1));
        assertEquals("CA_", variants.get(2));
    }

    @Test
    void differencesAreCorrect() {
        Word cat = new Word("cat");
        Word cot = new Word("cot");
        Word dog = new Word("dog");

        assertEquals(0, cat.differences(cat));
        assertEquals(0, cot.differences(cot));
        assertEquals(0, dog.differences(dog));

        assertEquals(1, cat.differences(cot));
        assertEquals(1, cot.differences(cat));

        assertEquals(2, cot.differences(dog));
        assertEquals(2, dog.differences(cot));

        assertEquals(3, cat.differences(dog));
        assertEquals(3, dog.differences(cat));
    }

    @Test
    void equalityCheck() {
        Word word1 = new Word("cat");
        Word word2 = new Word("CAT");
        assertTrue(word1.equals(word2));
        Word word3 = new Word("dog");
        assertFalse(word1.equals(word3));

        assertFalse(word1.equals(null));
        assertFalse(word1.equals(new Object()));
    }
}
