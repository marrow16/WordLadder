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

    public WordDistanceMap(Word word, Integer maximumLadderLength) {
        distances.put(word, 1);
        Queue<Word> queue = new ArrayDeque<>();
        queue.add(word);
        int maxDistance = maximumLadderLength != null ? maximumLadderLength : Integer.MAX_VALUE;
        while (!queue.isEmpty()) {
            Word nextWord = queue.remove();
            int distance = distances.getOrDefault(nextWord, 0) + 1;
            if (distance <= maxDistance) {
                nextWord.getLinkedWords().stream()
                        .filter(linkedWord -> !distances.containsKey(linkedWord))
                        .forEach(linkedWord -> {
                            queue.add(linkedWord);
                            distances.computeIfAbsent(linkedWord, w -> distance);
                        });
            }
        }
    }

    public WordDistanceMap(Word word) {
        this(word, null);
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
