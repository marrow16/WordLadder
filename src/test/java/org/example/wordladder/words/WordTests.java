package org.example.wordladder.words;

import org.example.wordladder.exceptions.BadWordException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class WordTests {
    @Test
    public void canCreateWord() {
        Word word = new Word("cat");
        assertEquals("CAT", word.toString());
    }

    @Test(expected = BadWordException.class)
    public void failsToCreateWordWithReservedChar() {
        new Word("c_t");
    }

    @Test
    public void variationPatternsAreCorrect() {
        Word word = new Word("cat");
        List<String> variants = word.getVariationPatterns();
        assertEquals(3, variants.size());
        assertEquals("_AT", variants.get(0));
        assertEquals("C_T", variants.get(1));
        assertEquals("CA_", variants.get(2));
    }

    @Test
    public void differencesAreCorrect() {
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
    public void equalityCheck() {
        Word word1 = new Word("cat");
        Word word2 = new Word("CAT");
        assertTrue(word1.equals(word2));
        Word word3 = new Word("dog");
        assertFalse(word1.equals(word3));

        assertFalse(word1.equals(null));
        assertFalse(word1.equals(new Object()));
    }
}
