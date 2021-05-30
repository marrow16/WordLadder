package org.example.wordladder.words;

import org.example.wordladder.exceptions.BadWordException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Word {
    private static final char VARIATION_CHAR = '_';

    private final String actualWord;
    private final char[] wordChars;
    private final int hashCode;
    private final List<Word> linkedWords = new ArrayList<>();

    Word(String actualWord) {
        if (actualWord.indexOf(VARIATION_CHAR) != -1) {
            throw new BadWordException("Word '" + actualWord + "' cannot contain reserved character '" + VARIATION_CHAR + "'");
        }
        this.actualWord = actualWord.toUpperCase();
        this.wordChars = this.actualWord.toCharArray();
        this.hashCode = generateHashCode();
    }

    private int generateHashCode() {
        return this.actualWord.hashCode();
    }

    List<String> getVariationPatterns() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < wordChars.length; i++) {
            char[] variantChars = wordChars.clone();
            variantChars[i] = VARIATION_CHAR;
            result.add(new String(variantChars));
        }
        return result;
    }

    void addLinkedWords(List<Word> variants) {
        this.linkedWords.addAll(
                variants.stream()
                        .filter(word -> word != this)
                        .collect(Collectors.toList())
        );
    }

    public List<Word> getLinkedWords() {
        return linkedWords;
    }

    public boolean isIslandWord() {
        return linkedWords.isEmpty();
    }

    public int differences(Word other) {
        int result = 0;
        for (int ch = 0; ch < wordChars.length; ch++) {
            result += (wordChars[ch] != other.wordChars[ch] ? 1 : 0);
        }
        return result;
    }

    public int similarities(Word other) {
        int result = 0;
        for (int ch = 0; ch < wordChars.length; ch++) {
            result += (wordChars[ch] == other.wordChars[ch] ? 1 : 0);
        }
        return result;
    }

    public int firstDifference(Word other) {
        int result = -1;
        for (int ch = 0; ch < wordChars.length; ch++) {
            if (wordChars[ch] != other.wordChars[ch]) {
                result = ch;
                break;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return actualWord;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !this.getClass().equals(other.getClass())) {
            return false;
        }
        return actualWord.equals(((Word)other).actualWord);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
