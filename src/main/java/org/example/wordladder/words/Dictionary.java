package org.example.wordladder.words;

import org.example.wordladder.exceptions.BadWordException;
import org.example.wordladder.exceptions.DictionaryLoadErrorException;
import org.example.wordladder.exceptions.NoResourceForDictionaryException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dictionary {
    private static final String RESOURCE_NAME_SUFFIX = "-letter-words.txt";

    private final int wordLength;
    private final Map<String, Word> words = new HashMap<>();

    public Dictionary(int wordLength) {
        this.wordLength = wordLength;
        loadWordsFromResources();
    }

    private void loadWordsFromResources() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(wordLength + RESOURCE_NAME_SUFFIX);
        if (resource == null) {
            throw new NoResourceForDictionaryException("Dictionary resource for word length "
                    + wordLength + " does not exist");
        }
        WordLinkageBuilder linkageBuilder = new WordLinkageBuilder();
        try {
            Files.lines(Paths.get(resource.toURI()))
                    .forEach(line -> addWord(line, linkageBuilder));
        } catch (IOException | URISyntaxException e) {
            throw new DictionaryLoadErrorException("Error loading " + wordLength + " letter word dictionary", e);
        }
    }

    private void addWord(String str, WordLinkageBuilder linkageBuilder) {
        if (!str.isEmpty()) {
            if (str.length() != wordLength) {
                throw new BadWordException("Word '" + str + "' (length = "
                        + str.length() + ") cannot be loaded into " + wordLength + " letter word dictionary");
            }
            Word word = new Word(str);
            words.put(word.toString(), word);
            linkageBuilder.link(word);
        }
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

    public int getWordLength() {
        return wordLength;
    }

    private static class WordLinkageBuilder {
        private final Map<String, List<Word>> variations = new HashMap<>();

        private void link(Word word) {
            word.getVariationPatterns().forEach(variation -> {
                List<Word> links = variations.computeIfAbsent(variation, s -> new ArrayList<>());
                links.forEach(linkedWord -> {
                    linkedWord.addLinkedWord(word);
                    word.addLinkedWord(linkedWord);
                });
                links.add(word);
            });
        }
    }

    public static class Factory {
        private static final Map<Integer, Dictionary> CACHE = new HashMap<>();

        public static Dictionary fromWord(String word) {
            return forWordLength(word.length());
        }

        public static Dictionary forWordLength(int wordLength) {
            return CACHE.computeIfAbsent(wordLength, integer -> new Dictionary(wordLength));
        }
    }
}
