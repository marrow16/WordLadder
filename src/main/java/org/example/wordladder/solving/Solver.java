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

    public Solver(Puzzle puzzle, Options options) {
        this.puzzle = puzzle;
        this.options = options;
    }

    public void solve() {
        exploredCount.set(0);
        solutions.clear();
        beginWord = puzzle.getStartWord();
        endWord = puzzle.getFinalWord();
        reversed = false;
        if (beginWord.equals(endWord)) {
            // same word - so there's only one solution...
            solutions.add(new Solution(beginWord));
        } else if (beginWord.differences(endWord) == 1) {
            // the two words are only one letter different - so there's only one solution needed (no point going round the houses!)...
            solutions.add(new Solution(beginWord, endWord));
        } else {
            // begin with the word that has the least number of linked words...
            // (this limits the number of pointless candidates explored!)
            reversed = beginWord.getLinkedWords().size() > endWord.getLinkedWords().size();
            if (reversed) {
                beginWord = puzzle.getFinalWord();
                endWord = puzzle.getStartWord();
            }
            beginWord.getLinkedWords()
                    .parallelStream()
                    .map(linkedWord -> new CandidateSolution(this, beginWord, linkedWord))
                    .forEach(this::solve);
        }
    }

    private void solve(CandidateSolution candidate) {
        Word lastWord = candidate.ladder.get(candidate.ladder.size() - 1);
        if (lastWord.equals(endWord)) {
            foundSolution(candidate);
        } else if (candidate.ladder.size() < options.getMaximumLadderLength()) {
            lastWord.getLinkedWords()
                    .parallelStream()
                    .filter(linkedWord -> !candidate.seenWords.contains(linkedWord))
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
        boolean inReverse = start.getLinkedWords().size() > end.getLinkedWords().size();
        if (inReverse) {
            end = puzzle.getStartWord();
            start = puzzle.getFinalWord();
        }
        Map<Word, Integer> distances = new HashMap<>();
        distances.put(start, 1);
        Queue<Word> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Word word = queue.remove();
            word.getLinkedWords().stream()
                    .filter(linkedWord -> !distances.containsKey(linkedWord))
                    .forEach(linkedWord -> {
                        queue.add(linkedWord);
                        distances.computeIfAbsent(linkedWord, w -> 1 + distances.get(word));
                    });
        }
        return Optional.ofNullable(distances.get(end));
    }

    public boolean isSolvable() {
        return calculateMinimumLadderLength().isPresent();
    }
}
