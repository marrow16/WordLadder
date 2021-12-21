package org.example.wordladder.solving;

import org.example.wordladder.words.Word;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public class WordDistanceMap {
    public final Map<Word, Integer> distances = new HashMap<>();

    public WordDistanceMap(Word word) {
        distances.put(word, 1);
        Queue<Word> queue = new ArrayDeque<>();
        queue.add(word);
        while (!queue.isEmpty()) {
            Word nextWord = queue.remove();
            nextWord.getLinkedWords().stream()
                    .filter(linkedWord -> !distances.containsKey(linkedWord))
                    .forEach(linkedWord -> {
                        queue.add(linkedWord);
                        distances.computeIfAbsent(linkedWord, w -> 1 + distances.get(nextWord));
                    });
        }
    }

    Optional<Integer> getDistance(Word toWord) {
        return Optional.ofNullable(distances.get(toWord));
    }

    boolean reachable(Word word, int maximumLadderLength) {
        int distance = distances.getOrDefault(word, -1);
        return distance != -1
                && distance <= maximumLadderLength;
    }

    boolean reachable(Word word, int maximumLadderLength, int currentLadderLength) {
        int distance = distances.getOrDefault(word, -1);
        return distance != -1
                && distance <= (maximumLadderLength - currentLadderLength);
    }
}
