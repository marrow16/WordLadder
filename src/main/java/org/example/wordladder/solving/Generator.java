package org.example.wordladder.solving;

import org.example.wordladder.words.Dictionary;
import org.example.wordladder.words.Word;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Generator {
    private static final Random RANDOM = new Random();

    private final int ladderLength;
    private final Dictionary dictionary;

    public Generator(int wordLength, int ladderLength) {
        this.ladderLength = ladderLength;
        dictionary = Dictionary.Factory.forWordLength(wordLength);
    }

    public List<Word> generate() {
        final Set<Word> result = new LinkedHashSet<>();
        Word onWord = randomWord();
        Word firstWord = onWord;
        Word previousWord = null;
        result.add(onWord);
        while (result.size() < ladderLength) {
            Word nextWord = randomNextWord(firstWord, previousWord, onWord, result);
            previousWord = onWord;
            onWord = nextWord;
            result.add(onWord);
        }
        return new ArrayList<>(result);
    }

    private Word randomWord() {
        List<Word> words = new ArrayList<>(dictionary.getWords());
        Word result = words.get(RANDOM.nextInt(words.size()));
        while (result.isIslandWord()) {
            result = words.get(RANDOM.nextInt(words.size()));
        }
        return result;
    }

    private Word randomNextWord(Word firstWord, Word previousWord, Word fromWord, Set<Word> seenWords) {
        final int changeAt = previousWord != null ? fromWord.firstDifference(previousWord) : -1;
        final int delta = firstWord.differences(fromWord);
        List<Word> candidates = fromWord.getLinkedWords().stream()
                .filter(word -> !word.isIslandWord() && !seenWords.contains(word))
                .filter(word -> changeAt == -1 || changeAt != word.firstDifference(fromWord))
                .filter(word -> word.differences(firstWord) >= delta)
                .toList();
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Oops! Sorry, couldn't generate word ladder - please retry");
        }
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }
}
