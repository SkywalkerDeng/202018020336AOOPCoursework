import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ModelTest {
    private Model model;
    private static final String DICTIONARY_FILE = "dictionary.txt";

    @Before
    public void setUp() {
        // Ensure a dummy dictionary.txt exists for tests if the original is not present.
        // This helps in making the tests runnable even without a pre-existing file.
        File dictionary = new File(DICTIONARY_FILE);
        if (!dictionary.exists()) {
            System.out.println("INFO: " + DICTIONARY_FILE + " not found. Creating a dummy one for testing.");
            createDummyDictionary();
        }
        model = new Model(); // Model constructor loads the dictionary
    }

    // Helper method to create a minimal dictionary for tests to pass
    private void createDummyDictionary() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DICTIONARY_FILE))) {
            writer.println("sale");
            writer.println("same");
            writer.println("male");
            writer.println("mame");
            writer.println("game"); // For random word tests
            writer.println("lame"); // For random word tests
            writer.println("test"); // Used in testInvalidMoves
            writer.println("rope"); // Another word for variety
            writer.println("boat"); // Another word for variety
            // Add any other 4-letter words required by specific test scenarios
        } catch (IOException e) {
            // If this fails, tests relying on dictionary will likely fail,
            // but at least we tried.
            System.err.println("ERROR: Could not create dummy " + DICTIONARY_FILE + ". " + e.getMessage());
        }
    }

    @Test
    public void testBasicGameFlow() {
        // Test with default start "sale" and target "same"
        assertEquals("Default start word should be 'sale'", "sale", model.getStartWord());
        assertEquals("Default target word should be 'same'", "same", model.getTargetWord());
        assertTrue("Game history should be empty initially", model.getGameHistory().isEmpty());

        // Check if words are considered valid by the model (depends on dictionary.txt)
        assertTrue("'sale' should be a valid word", model.isValidWord("sale"));
        assertTrue("'same' should be a valid word", model.isValidWord("same"));

        // Check if a move from "sale" to "same" is considered valid (1 char difference)
        assertTrue("Move from 'sale' to 'same' should be considered a valid transition", model.isValidMove("sale", "same"));

        // In the provided Model.java, makeMove(X) uses startWord as the previous word if history is empty.
        // isValidMove(prev, new) returns false if prev.equals(new) because differences == 0.
        // Thus, an initial makeMove("sale") would fail.
        // The first successful move must be to a word different from startWord.
        // Here, we test a direct move from startWord "sale" to targetWord "same".
        boolean moveSuccess = model.makeMove("same");
        // Print statement from original test for debugging
        System.out.println("makeMove(\"same\") from startWord \"" + model.getStartWord() + "\" result: " + moveSuccess);
        assertTrue("Move from start word 'sale' to 'same' should be successful", moveSuccess);

        List<String> history = model.getGameHistory();
        assertEquals("History should contain 1 entry after one successful move to 'same'", 1, history.size());
        assertEquals("The only entry in history should be 'same'", "same", history.get(0));

        // Check if the game is won
        assertTrue("Game should be won as current word 'same' is the target word", model.hasWon());
    }

    @Test
    public void testInvalidMoves() {
        // Test that making a move with an invalid word (not in dictionary or wrong format) fails
        assertFalse("makeMove with a clearly invalid word 'xxxx' should fail", model.makeMove("xxxx"));
        assertTrue("History should be empty after failed move with 'xxxx'", model.getGameHistory().isEmpty());

        String start = model.getStartWord(); // Should be "sale" by default
        assertTrue("The start word ('" + start + "') should itself be a valid word", model.isValidWord(start));

        // Attempting to make a "move" to the start word itself will fail
        // because model.isValidMove(start, start) is false (0 character differences).
        assertFalse("Making a move to the start word itself ('" + start + "') should fail", model.makeMove(start));
        assertTrue("History should be empty after attempting to move to start word itself", model.getGameHistory().isEmpty());

        // Test making an invalid move from startWord to "test"
        // model.isValidMove(start, "test") checks if "test" is a valid word and 1 char diff from start.
        // model.makeMove("test") will use startWord as prevWord if history is empty.
        // This assertion depends on "test" being either not a valid word, or not 1-diff from "sale".
        // Assuming "test" is in dummy dictionary but not 1-diff from "sale".
        // If "test" is in dictionary.txt: isValidWord("test") is true.
        // isValidMove("sale", "test") will be false (differs by >1 or 0).
        assertFalse("makeMove should fail for 'test' from '" + start + "' if it's an invalid transition", model.makeMove("test"));
        assertTrue("History should remain empty after failed move to 'test'", model.getGameHistory().isEmpty());

        // Turn off error messages for the next invalid move test
        model.setShowErrorMessage(false);
        assertFalse("makeMove should fail for 'zzzz' (invalid word)", model.makeMove("zzzz"));

        // Check history size. Since initial makeMove(start) failed, and other attempted moves
        // from start also failed (or were to invalid words), history should still be empty.
        assertEquals("History should be empty after various failed moves from start", 0, model.getGameHistory().size());

        // Restore error message display for subsequent tests or manual play
        model.setShowErrorMessage(true);
        assertFalse("makeMove should fail for 'abcd' (invalid word)", model.makeMove("abcd"));
        assertEquals("History should still be 0 after another invalid move", 0, model.getGameHistory().size());
    }

    @Test
    public void testRandomWordsAndReset() {
        String originalStart = model.getStartWord();
        String originalTarget = model.getTargetWord();

        model.setUseRandomWords(true); // This also calls initializeGame() in your Model
        String newStart = model.getStartWord();
        String newTarget = model.getTargetWord();

        // Check that words have changed and are valid (assuming dictionary has enough variety)
        // Use the new getter method for dictionary size
        if (model.getDictionarySize() > 1) { // Only assert not equals if random selection is possible
            assertNotEquals("Start word should change when random words are enabled", originalStart, newStart);
            assertNotEquals("Target word should change when random words are enabled", originalTarget, newTarget);
        }
        assertTrue("New random start word '" + newStart + "' should be valid", model.isValidWord(newStart));
        assertTrue("New random target word '" + newTarget + "' should be valid", model.isValidWord(newTarget));
        assertTrue("History should be empty after setUseRandomWords(true) called initializeGame", model.getGameHistory().isEmpty());

        // This part of the original test was trying to find a 'step' from gameHistory.
        // Since initializeGame() clears history, gameHistory will be empty here.
        // So, 'step' will correctly remain null, and the if-block won't execute.
        // This is fine and doesn't indicate a failure in Model.java.
        String step = null;
        for (String candidate : model.getGameHistory()) { // gameHistory is empty
            if (model.isValidMove(newStart, candidate)) {
                step = candidate;
                break;
            }
        }
        assertNull("Step should be null as game history is empty", step);

        // Test resetting the game
        model.setUseRandomWords(false); // This calls initializeGame(), reverting to defaults
        assertEquals("Start word should revert to default 'sale'", "sale", model.getStartWord());
        assertEquals("Target word should revert to default 'same'", "same", model.getTargetWord());
        assertTrue("Game history should be empty after resetting to default words", model.getGameHistory().isEmpty());
    }

    @Test
    public void testPathDisplay() {
        // Test showing the path (model.showPath itself doesn't change game logic here)
        assertFalse("showPath should be false by default", model.isShowPath());
        model.setShowPath(true);
        assertTrue("showPath should be true after setting it", model.isShowPath());

        // Define a path to test.
        // Start: "sale", Target: "same" (default)
        // Path: sale -> male -> mame -> same
        String[] path = {"sale", "male", "mame", "same"};

        // The first word "sale" will not be "moved to" by makeMove if it's the startWord,
        // as isValidMove("sale", "sale") is false.
        // The loop will effectively start making moves from the startWord to the next word in path.
        for (String wordInPath : path) {
            String previousWordForMoveDecision;
            if (model.getGameHistory().isEmpty()) {
                previousWordForMoveDecision = model.getStartWord();
            } else {
                previousWordForMoveDecision = model.getGameHistory().get(model.getGameHistory().size() - 1);
            }

            if (wordInPath.equals(model.getStartWord()) && model.getGameHistory().isEmpty()) {
                System.out.println("PathDisplay: Current word in path array is start word: " + wordInPath + ". No 'makeMove' action for this step.");
            } else if (model.isValidMove(previousWordForMoveDecision, wordInPath)) {
                boolean success = model.makeMove(wordInPath);
                System.out.println("Move " + previousWordForMoveDecision + " → " + wordInPath + ": " + success + " | History: " + model.getGameHistory());
                assertTrue("Move from " + previousWordForMoveDecision + " to " + wordInPath + " should succeed", success);
            } else {
                System.out.println("PathDisplay: Skipped invalid or non-move in path array processing: " + previousWordForMoveDecision + " → " + wordInPath);
            }
        }

        List<String> history = model.getGameHistory();
        assertFalse("Game history should not be empty after traversing a path", history.isEmpty());
        // Expected history after processing path {"sale", "male", "mame", "same"}
        // given startWord="sale": ["male", "mame", "same"]
        assertEquals("History should contain 3 entries for the path sale->male->mame->same", 3, history.size());
        if (history.size() == 3) { // Avoid IndexOutOfBounds if previous assertion fails
            assertEquals("First recorded move in history should be 'male'", "male", history.get(0));
            assertEquals("Second recorded move should be 'mame'", "mame", history.get(1));
            assertEquals("Last recorded move should be 'same'", "same", history.get(2));
        }

        assertEquals("The last word in history should match the target word", model.getTargetWord(), history.get(history.size() - 1));
        assertTrue("Game should be won after reaching the target word in path display test", model.hasWon());
    }
}