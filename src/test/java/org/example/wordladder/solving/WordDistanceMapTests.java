package org.example.wordladder.solving;

import org.example.wordladder.words.Dictionary;
import org.example.wordladder.words.Word;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordDistanceMapTests {
    @Test
    void islandWordHasLimitedMap() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("iwi");
        WordDistanceMap wordDistanceMap = new WordDistanceMap(word);
        assertEquals(1, wordDistanceMap.distances.size());
        assertTrue(wordDistanceMap.distances.containsKey(word));
    }

    @Test
    void catMap() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("cat");
        WordDistanceMap wordDistanceMap = new WordDistanceMap(word);

        assertEquals(1346, wordDistanceMap.distances.size());
        assertTrue(wordDistanceMap.distances.containsKey(word));
        assertEquals(1, wordDistanceMap.distances.get(word));

        Word endWord = dictionary.getWord("dog");
        assertTrue(wordDistanceMap.distances.containsKey(endWord));

        assertTrue(wordDistanceMap.reachable(endWord, 5));
        assertTrue(wordDistanceMap.reachable(endWord, 4));
        assertFalse(wordDistanceMap.reachable(endWord, 3));
        assertFalse(wordDistanceMap.reachable(endWord, 2));
    }

    @Test
    void catMapLimited() {
        Dictionary dictionary = Dictionary.Factory.forWordLength(3);
        Word word = dictionary.getWord("cat");
        WordDistanceMap wordDistanceMap = new WordDistanceMap(word, 4);

        assertEquals(1086, wordDistanceMap.distances.size());
        assertTrue(wordDistanceMap.distances.containsKey(word));
        assertEquals(1, wordDistanceMap.distances.get(word));

        Word endWord = dictionary.getWord("dog");
        assertTrue(wordDistanceMap.distances.containsKey(endWord));

        assertTrue(wordDistanceMap.reachable(endWord, 5));
        assertTrue(wordDistanceMap.reachable(endWord, 4));
        assertFalse(wordDistanceMap.reachable(endWord, 3));
        assertFalse(wordDistanceMap.reachable(endWord, 2));

        // limit further...
        wordDistanceMap = new WordDistanceMap(word, 3);
        assertEquals(345, wordDistanceMap.distances.size());
        assertFalse(wordDistanceMap.distances.containsKey(endWord));
    }
}
