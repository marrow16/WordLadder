package org.example.wordladder.solving;

import org.example.wordladder.words.Dictionary;
import org.example.wordladder.words.Word;

import java.util.*;
import java.util.stream.Collectors;

public class Generator {
    private final int wordLength;
    private final int ladderLength;
    private final Dictionary dictionary;

    public Generator(int wordLength, int ladderLength) {
        this.wordLength = wordLength;
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
        Random random = new Random();
        Word result = words.get(random.nextInt(words.size()));
        while (result.isIslandWord()) {
            result = words.get(random.nextInt(words.size()));
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
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Oops! Sorry, couldn't generate word ladder - please retry");
        }
        Random random = new Random();
        return candidates.get(random.nextInt(candidates.size()));
    }
}
