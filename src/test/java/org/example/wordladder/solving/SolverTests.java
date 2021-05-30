package org.example.wordladder.solving;

import org.example.wordladder.Puzzle;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SolverTests {
    @Test
    public void solveCatToDog() {
        Options options = new Options();
        options.setMaximumLadderLength(4);

        Puzzle puzzle = new Puzzle("cat", "dog");
        Solver solver = new Solver(puzzle, options);
        solver.solve();

        List<Solution> solutions = solver.getSolutions();
        assertEquals(4, solutions.size());
        Map<Integer, Set<String>> midWords = new HashMap<>();
        for (Solution solution: solutions) {
            assertEquals(4, solution.getLadder().size());
            assertEquals("CAT", solution.getLadder().get(0).toString());
            assertEquals("DOG", solution.getLadder().get(3).toString());
            midWords.computeIfAbsent(1, integer -> new HashSet<>()).add(solution.getLadder().get(1).toString());
            midWords.computeIfAbsent(2, integer -> new HashSet<>()).add(solution.getLadder().get(2).toString());
        }
        assertEquals(2, midWords.get(1).size());
        assertTrue(midWords.get(1).contains("CAG"));
        assertTrue(midWords.get(1).contains("COT"));
        assertEquals(3, midWords.get(2).size());
        assertTrue(midWords.get(2).contains("DOT"));
        assertTrue(midWords.get(2).contains("COG"));
        assertTrue(midWords.get(2).contains("DAG"));
    }

    @Test
    public void minimumLadderForCatToDog() {
        Options options = new Options();

        Puzzle puzzle = new Puzzle("cat", "dog");
        Solver solver = new Solver(puzzle, options);

        Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
        assertTrue(minimumLadderLength.isPresent());
        assertEquals((Integer)4, minimumLadderLength.get());
    }

    @Test
    public void minimumLadderForColdToWarm() {
        Options options = new Options();

        Puzzle puzzle = new Puzzle("cold", "warm");
        Solver solver = new Solver(puzzle, options);

        Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
        assertTrue(minimumLadderLength.isPresent());
        assertEquals((Integer)5, minimumLadderLength.get());
    }

    @Test
    public void solveKataToJava() {
        Options options = new Options();
        options.setMaximumLadderLength(3);

        Puzzle puzzle = new Puzzle("kata", "java");

        Solver solver = new Solver(puzzle, options);

        solver.solve();

        List<Solution> solutions = solver.getSolutions();
        assertEquals(1, solutions.size());

        Solution solution = solutions.get(0);
        assertEquals("KATA", solution.getLadder().get(0).toString());
        assertEquals("KAVA", solution.getLadder().get(1).toString());
        assertEquals("JAVA", solution.getLadder().get(2).toString());
    }

    @Test
    public void minimumLadderForKataToJava() {
        Options options = new Options();

        Puzzle puzzle = new Puzzle("kata", "java");

        Solver solver = new Solver(puzzle, options);

        Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
        assertTrue(minimumLadderLength.isPresent());
        assertEquals((Integer)3, minimumLadderLength.get());
    }

    @Test
    public void cannotSolveLlamaToArtsy() {
        Options options = new Options();

        Puzzle puzzle = new Puzzle("llama", "artsy");

        Solver solver = new Solver(puzzle, options);

        Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
        assertFalse(minimumLadderLength.isPresent());
    }

}
