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
    public void solveColdToWarmAndWarmToCold() {
        Options options = new Options();
        options.setMaximumLadderLength(5);

        Puzzle puzzle = new Puzzle("cold", "warm");
        Solver solver = new Solver(puzzle, options);

        solver.solve();

        List<Solution> solutions = solver.getSolutions();
        assertEquals(7, solutions.size());
        long explored1 = solver.getExploredCount();

        // now do it the other way around..
        puzzle = new Puzzle("warm", "cold");
        solver = new Solver(puzzle, options);
        solver.solve();
        solutions = solver.getSolutions();
        assertEquals(7, solutions.size());
        assertEquals(explored1, solver.getExploredCount());
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

        // do it again using short-cut method
        assertFalse(solver.isSolvable());
    }

    @Test
    public void sameWordIsSolvable() {
        Options options = new Options();
        Puzzle puzzle = new Puzzle("cat", "cat");
        Solver solver = new Solver(puzzle, options);

        solver.solve();
        List<Solution> solutions = solver.getSolutions();
        assertEquals(1, solutions.size());
        assertEquals(0, solver.getExploredCount());
    }

    @Test
    public void oneLetterDifferenceIsSolvable() {
        Options options = new Options();
        options.setMaximumLadderLength(2);
        Puzzle puzzle = new Puzzle("cat", "cot");
        Solver solver = new Solver(puzzle, options);

        solver.solve();
        List<Solution> solutions = solver.getSolutions();
        assertEquals(1, solutions.size());
        assertEquals(0, solver.getExploredCount());
    }

    @Test
    public void twoLettersDifferenceIsSolvable() {
        Options options = new Options();
        options.setMaximumLadderLength(3);
        Puzzle puzzle = new Puzzle("cat", "bar");
        Solver solver = new Solver(puzzle, options);

        solver.solve();
        List<Solution> solutions = solver.getSolutions();
        assertEquals(2, solutions.size());
        assertEquals(0, solver.getExploredCount());
    }

    @Test
    public void shortCircuitsOnGetMaxLadderLength() {
        Options options = new Options();
        Puzzle puzzle = new Puzzle("cat", "bar");
        Solver solver = new Solver(puzzle, options);
        Optional<Integer> maximumLadderLength = solver.calculateMinimumLadderLength();
        assertTrue(maximumLadderLength.isPresent());
        assertEquals((Integer)3, maximumLadderLength.get());

        puzzle = new Puzzle("cat", "bat");
        solver = new Solver(puzzle, options);
        maximumLadderLength = solver.calculateMinimumLadderLength();
        assertTrue(maximumLadderLength.isPresent());
        assertEquals((Integer)2, maximumLadderLength.get());

        puzzle = new Puzzle("cat", "cat");
        solver = new Solver(puzzle, options);
        maximumLadderLength = solver.calculateMinimumLadderLength();
        assertTrue(maximumLadderLength.isPresent());
        assertEquals((Integer)1, maximumLadderLength.get());
    }

    @Test
    public void everythingUnsolvableWithBadMaxLadderLength() {
        Options options = new Options();
        options.setMaximumLadderLength(0);
        Puzzle puzzle = new Puzzle("cat", "dog");
        Solver solver = new Solver(puzzle, options);

        solver.solve();
        List<Solution> solutions = solver.getSolutions();
        assertEquals(0, solutions.size());
        assertEquals(0, solver.getExploredCount());
    }

    @Test
    public void optimizedSmartToMoney() {
        Options options = new Options();
        Puzzle puzzle = new Puzzle("smart", "money");
        Solver solver = new Solver(puzzle, options);

        Optional<Integer> minimumLadderLength = solver.calculateMinimumLadderLength();
        assertTrue(minimumLadderLength.isPresent());
        assertEquals((Integer)10, minimumLadderLength.get());

        options.setMaximumLadderLength(minimumLadderLength.get());
        solver.solve();
        List<Solution> solutions = solver.getSolutions();
        assertEquals(24, solutions.size());

        options.setMaximumLadderLength(minimumLadderLength.get() - 1);
        solver.solve();
        solutions = solver.getSolutions();
        assertTrue(solutions.isEmpty());
    }
}
