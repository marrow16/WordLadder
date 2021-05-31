package org.example.wordladder.solving;

import org.example.wordladder.Puzzle;
import org.example.wordladder.words.Word;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Solver {
    private final Puzzle puzzle;
    private final Options options;
    private final AtomicLong exploredCount = new AtomicLong();
    private final List<Solution> solutions = new ArrayList<>();
    private Word beginWord;
    private Word endWord;
    private boolean reversed;
    private Map<Word, Integer> endDistances;

    public Solver(Puzzle puzzle, Options options) {
        this.puzzle = puzzle;
        this.options = options;
    }

    public void solve() {
        exploredCount.set(0);
        solutions.clear();
        if (options.getMaximumLadderLength() < 1) {
            // won't find any solutions with ladder of length 0!...
            return;
        }
        beginWord = puzzle.getStartWord();
        endWord = puzzle.getFinalWord();
        reversed = false;
        // check for short-circuits...
        int differences = beginWord.differences(endWord);
        switch (differences) {
            case 0:
                // same word - so there's only one solution...
                solutions.add(new Solution(beginWord));
                return;
            case 1:
                // the two words are only one letter different...
                if (options.getMaximumLadderLength() == 2) {
                    // maximum ladder is 2 so we already have the only answer...
                    solutions.add(new Solution(beginWord, endWord));
                    return;
                }
            case 2:
                if (options.getMaximumLadderLength() == 3) {
                    // the two words are only two letters different and maximum ladder is 3...
                    // so we can determine solutions by convergence of the two linked word sets...
                    Set<Word> startLinkedWords = new HashSet<>(beginWord.getLinkedWords());
                    startLinkedWords.retainAll(endWord.getLinkedWords());
                    for (Word intermediateWord: startLinkedWords) {
                        solutions.add(new Solution(beginWord, intermediateWord, endWord));
                    }
                    return;
                }
        }
        // begin with the word that has the least number of linked words...
        // (this limits the number of pointless candidates explored!)
        reversed = beginWord.getLinkedWords().size() > endWord.getLinkedWords().size();
        if (reversed) {
            beginWord = puzzle.getFinalWord();
            endWord = puzzle.getStartWord();
        }
        endDistances = buildDistances(endWord);
        beginWord.getLinkedWords()
                .parallelStream()
                .filter(linkedWord -> endDistances.containsKey(linkedWord))
                .filter(linkedWord -> endDistances.get(linkedWord) <= options.getMaximumLadderLength())
                .map(linkedWord -> new CandidateSolution(this, beginWord, linkedWord))
                .forEach(this::solve);
    }

    private void solve(CandidateSolution candidate) {
        Word lastWord = candidate.ladder.get(candidate.ladder.size() - 1);
        if (lastWord.equals(endWord)) {
            foundSolution(candidate);
        } else if (candidate.ladder.size() < options.getMaximumLadderLength()) {
            lastWord.getLinkedWords()
                    .parallelStream()
                    .filter(linkedWord -> !candidate.seenWords.contains(linkedWord))
                    .filter(linkedWord -> endDistances.containsKey(linkedWord))
                    .filter(linkedWord -> (endDistances.get(linkedWord) + candidate.ladder.size()) <= options.getMaximumLadderLength())
                    .map(linkedWord -> new CandidateSolution(candidate, linkedWord))
                    .forEach(this::solve);
        }
    }

    synchronized private void foundSolution(CandidateSolution candidate) {
        Solution solution = new Solution(candidate, reversed);
        solutions.add(solution);
    }

    synchronized void incrementExplored() {
        exploredCount.incrementAndGet();
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public long getExploredCount() {
        return exploredCount.get();
    }

    public Optional<Integer> calculateMinimumLadderLength() {
        Word start = puzzle.getStartWord();
        Word end = puzzle.getFinalWord();
        // check for short-circuits...
        int differences = start.differences(end);
        switch (differences) {
            case 0:
            case 1:
                return Optional.of(differences + 1);
            case 2:
                Set<Word> startLinkedWords = new HashSet<>(start.getLinkedWords());
                startLinkedWords.retainAll(end.getLinkedWords());
                if (!startLinkedWords.isEmpty()) {
                    return Optional.of(3);
                }
                break;
        }
        if (start.getLinkedWords().size() > end.getLinkedWords().size()) {
            // swap start and end word...
            end = puzzle.getStartWord();
            start = puzzle.getFinalWord();
        }
        Map<Word, Integer> distances = buildDistances(start);
        return Optional.ofNullable(distances.get(end));
    }

    public boolean isSolvable() {
        return calculateMinimumLadderLength().isPresent();
    }

    private Map<Word, Integer> buildDistances(Word word) {
        Map<Word, Integer> result = new HashMap<>();
        result.put(word, 1);
        Queue<Word> queue = new ArrayDeque<>();
        queue.add(word);
        while (!queue.isEmpty()) {
            Word nextWord = queue.remove();
            nextWord.getLinkedWords().stream()
                    .filter(linkedWord -> !result.containsKey(linkedWord))
                    .forEach(linkedWord -> {
                        queue.add(linkedWord);
                        result.computeIfAbsent(linkedWord, w -> 1 + result.get(nextWord));
                    });
        }
        return result;
    }
}
