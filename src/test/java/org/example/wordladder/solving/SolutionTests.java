package org.example.wordladder.solving;

import org.example.wordladder.words.Dictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SolutionTests {
    private Dictionary dictionary;

    @BeforeEach
    void before() {
        dictionary = Dictionary.Factory.forWordLength(3);
    }

    @Test
    void constructors() {
        Solution solution = new Solution(dictionary.getWord("cat"), dictionary.getWord("bat"));
        assertNotNull(solution);
    }

    @Test
    void checkSize() {
        Solution solution = new Solution(dictionary.getWord("cat"), dictionary.getWord("bat"));
        assertEquals(2, solution.size());
    }

    @Test
    void checkToString() {
        Solution solution = new Solution(dictionary.getWord("cat"), dictionary.getWord("bat"));
        assertEquals("[CAT, BAT]", solution.toString());
    }
}
