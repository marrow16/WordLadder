package org.example.wordladder.solving;

import org.example.wordladder.Puzzle;
import org.example.wordladder.words.Word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class Solver {
    private final Puzzle puzzle;
    private final Options options;
    private final AtomicLong exploredCount = new AtomicLong();
    private final List<Solution> solutions = new ArrayList<>();
    private Word beginWord;
    private Word endWord;
    private boolean reversed;

    private int maximumLadderLength;
    private WordDistanceMap endDistances;

    public Solver(Puzzle puzzle, Options options) {
        this.puzzle = puzzle;
        this.options = options;
    }

    public void solve() {
        exploredCount.set(0);
        solutions.clear();
        maximumLadderLength = options.getMaximumLadderLength();
        if (maximumLadderLength < 1) {
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
                solutions.add(new Solution(beginWord, endWord));
                if (maximumLadderLength == 2) {
                    // maximum ladder is 2 so we already have the only answer...
                    return;
                }
            case 2:
                if (maximumLadderLength == 3) {
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
        endDistances = new WordDistanceMap(endWord);
        beginWord.getLinkedWords()
                .parallelStream()
                .filter(word -> endDistances.reachable(word, maximumLadderLength))
                .map(linkedWord -> new CandidateSolution(this, beginWord, linkedWord))
                .forEach(this::solve);
    }

    private void solve(CandidateSolution candidate) {
        Word lastWord = candidate.ladder.get(candidate.ladder.size() - 1);
        if (lastWord.equals(endWord)) {
            foundSolution(candidate);
        } else if (candidate.ladder.size() < maximumLadderLength) {
            lastWord.getLinkedWords()
                    .parallelStream()
                    .filter(linkedWord -> !candidate.seenWords.contains(linkedWord) && endDistances.reachable(linkedWord, maximumLadderLength, candidate.ladder.size()))
                    .map(linkedWord -> new CandidateSolution(candidate, linkedWord))
                    .forEach(this::solve);
        }
    }

    private synchronized void foundSolution(CandidateSolution candidate) {
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
            case 0, 1:
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
        return new WordDistanceMap(start).getDistance(end);
    }

    public boolean isSolvable() {
        return calculateMinimumLadderLength().isPresent();
    }
}
