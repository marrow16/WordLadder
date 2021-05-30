package org.example.wordladder.solving;

import org.example.wordladder.words.Word;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GeneratorTests {
    @Test
    public void canGeneratePuzzle() {
        Generator generator = new Generator(4, 5);

        boolean success = false;
        int retry = 0;
        List<Word> words = null;
        while (!success && retry < 10) {
            try {
                words = generator.generate();
                success = true;
            } catch (Exception e) {
                retry++;
            }
        }
        assertNotNull(words);
        assertEquals(4, words.get(0).toString().length());
        assertEquals(5, words.size());
    }
}
