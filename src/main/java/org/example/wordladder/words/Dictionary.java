package org.example.wordladder.words;

import org.example.wordladder.exceptions.BadWordException;
import org.example.wordladder.exceptions.DictionaryLoadErrorException;
import org.example.wordladder.exceptions.NoResourceForDictionaryException;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Dictionary {
    private static final String RESOURCE_NAME_SUFFIX = "-letter-words.txt";

    private final int wordLength;
    private final Map<String, Word> words = new HashMap<>();

    public Dictionary(int wordLength) {
        this.wordLength = wordLength;
        loadWordsFromResources();
        buildWordVariations();
    }

    private void loadWordsFromResources() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(wordLength + RESOURCE_NAME_SUFFIX);
        if (resource == null) {
            throw new NoResourceForDictionaryException("Dictionary resource for word length " + wordLength + " does not exist");
        }
        try {
            Files.lines(Paths.get(resource.toURI()))
                    .forEach(this::addWord);
        } catch (IOException | URISyntaxException e) {
            throw new DictionaryLoadErrorException("Error loading " + wordLength + " letter word dictionary", e);
        }
    }

    private void addWord(String word) {
        if (!word.isEmpty()) {
            if (word.length() != wordLength) {
                throw new BadWordException("Word '" + word + "' (length = " + word.length() + ") cannot be loaded into " + wordLength + " letter word dictionary");
            }
            String upperWord = word.toUpperCase();
            words.put(upperWord, new Word(upperWord));
        }
    }

    private void buildWordVariations() {
        Map<String, List<Word>> variations = new HashMap<>();
        words.values()
                .forEach(word -> word.getVariationPatterns()
                        .forEach(variationPattern -> variations.computeIfAbsent(variationPattern, s -> new ArrayList<>()).add(word)));

        variations.values()
                .forEach(wordVariants -> wordVariants.forEach(word -> word.addLinkedWords(wordVariants)));
    }

    public boolean isEmpty() {
        return words.isEmpty();
    }

    public int size() {
        return words.size();
    }

    public Word getWord(String word) {
        return words.get(word.toUpperCase());
    }

    public Collection<Word> getWords() {
        return words.values();
    }

    public int getWordLength() {
        return wordLength;
    }

    public static class Factory {
        private static final Map<Integer, Dictionary> cache = new HashMap<>();

        public static Dictionary fromWord(String word) {
            return forWordLength(word.length());
        }

        public static Dictionary forWordLength(int wordLength) {
            return cache.computeIfAbsent(wordLength, integer -> new Dictionary(wordLength));
        }
    }
}
