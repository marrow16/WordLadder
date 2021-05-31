package org.example.wordladder.solving;

import org.example.wordladder.words.Dictionary;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SolutionTests {
    private Dictionary dictionary;

    @Before
    public void before() {
        dictionary = Dictionary.Factory.forWordLength(3);
    }

    @Test
    public void constructors() {
        Solution solution = new Solution(dictionary.getWord("cat"), dictionary.getWord("bat"));
        assertNotNull(solution);
    }

    @Test
    public void checkSize() {
        Solution solution = new Solution(dictionary.getWord("cat"), dictionary.getWord("bat"));
        assertEquals(2, solution.size());
    }

    @Test
    public void checkToString() {
        Solution solution = new Solution(dictionary.getWord("cat"), dictionary.getWord("bat"));
        assertEquals("[CAT, BAT]", solution.toString());
    }
}
