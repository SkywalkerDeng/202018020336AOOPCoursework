import java.util.Observable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

public class Model extends Observable {

    public static enum FeedbackState {
        CORRECT_POSITION,
        NOT_IN_WORD
    }

    public static class LetterFeedback {
        public final char letter;
        public final FeedbackState state;

        public LetterFeedback(char letter, FeedbackState state) {
            this.letter = letter;
            this.state = state;
        }

        @Override
        public String toString() {
            return "{" + letter + "," + state + "}";
        }
    }

    private String startWord;
    private String targetWord;
    private Set<String> dictionary;
    private List<String> gameHistory;
    private boolean showErrorMessage;
    private boolean showPath;
    private boolean useRandomWords;
    private LetterFeedback[] lastGuessFeedback;

    public Model() {
        System.out.println("DEBUG Model Constructor: Initializing Model...");
        dictionary = loadDictionary("dictionary.txt");
        gameHistory = new ArrayList<>();
        showErrorMessage = true;
        showPath = false;
        useRandomWords = false;
        initializeGame();
        System.out.println("DEBUG Model Constructor: Model initialized. Dictionary size: " + (dictionary != null ? dictionary.size() : "null or not loaded") + ", showPath initial: " + this.showPath);
    }

    private Set<String> loadDictionary(String filename) {
        System.out.println("DEBUG loadDictionary: Attempting to load dictionary from file: " + filename);
        Set<String> words = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int loadedCount = 0;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 4) {
                    words.add(line.toLowerCase().trim());
                    loadedCount++;
                }
            }
            System.out.println("DEBUG loadDictionary: Successfully loaded " + loadedCount + " 4-letter words. Total unique words in set: " + words.size());
        } catch (IOException e) {
            System.err.println("ERROR loading dictionary: " + e.getMessage());
            e.printStackTrace();
        }
        if (words.isEmpty()) {
            System.out.println("DEBUG loadDictionary: Dictionary set is empty after attempting to load.");
        }
        return words;
    }

    public void initializeGame() {
        System.out.println("DEBUG initializeGame: Initializing game. useRandomWords: " + useRandomWords);
        if (useRandomWords) {
            System.out.println("DEBUG initializeGame: Using random words.");
            if (dictionary == null || dictionary.isEmpty()) {
                System.err.println("ERROR initializeGame: Dictionary is empty or not loaded. Cannot set random words.");
                startWord = "err_";
                targetWord = "dict";
                gameHistory.clear();
                lastGuessFeedback = null;
                setChanged();
                notifyObservers("reset_error_dict");
                return;
            }
            Random rand = new Random();
            int dictSize = dictionary.size();
            if (dictSize == 0) {
                System.err.println("ERROR initializeGame: Dictionary size is 0. Cannot pick random words.");
                startWord = "zero";
                targetWord = "size";
                gameHistory.clear();
                lastGuessFeedback = null;
                setChanged();
                notifyObservers("reset_error_dict_empty");
                return;
            }
            List<String> dictList = new ArrayList<>(dictionary);
            startWord = dictList.get(rand.nextInt(dictSize));
            if (dictSize > 1) {
                do {
                    targetWord = dictList.get(rand.nextInt(dictSize));
                } while (startWord.equals(targetWord));
            } else {
                targetWord = startWord;
                System.out.println("DEBUG initializeGame: Dictionary has only one word. Start and target will be the same: " + startWord);
            }
        } else {
            System.out.println("DEBUG initializeGame: Using default words.");
            startWord = "sale";
            targetWord = "same";
        }
        System.out.println("DEBUG initializeGame: Start word set to: '" + startWord + "', Target word set to: '" + targetWord + "'");
        gameHistory.clear();
        lastGuessFeedback = null;
        System.out.println("DEBUG initializeGame: Game history cleared.");
        setChanged();
        notifyObservers("reset");
    }

    public boolean isValidWord(String word) {
        if (word == null) return false;
        if (word.length() != 4) return false;
        String lowerCaseWord = word.toLowerCase();
        if (dictionary == null || dictionary.isEmpty()) return false;
        boolean found = dictionary.contains(lowerCaseWord);
        // Limit logging during pathfinding unless the word is not found or showPath is explicitly on.
        if (isShowPath() || !found || Thread.currentThread().getStackTrace()[2].getMethodName().equals("makeMove") || Thread.currentThread().getStackTrace()[2].getMethodName().equals("isValidMove")) {
            System.out.println("DEBUG isValidWord: Validating '" + lowerCaseWord + "'. Length is 4. Dict size: " + dictionary.size() + ". Found: " + found);
        }
        return found;
    }

    public boolean isValidMove(String prevWord, String newWord) {
        if (prevWord == null || newWord == null) return false;
        if (!isValidWord(newWord)) return false;
        if (prevWord.length() != 4) return false;

        String lowerPrevWord = prevWord.toLowerCase();
        String lowerNewWord = newWord.toLowerCase();
        int differences = 0;
        for (int i = 0; i < 4; i++) {
            if (lowerPrevWord.charAt(i) != lowerNewWord.charAt(i)) {
                differences++;
            }
        }
        return differences == 1;
    }

    public LetterFeedback[] checkGuess(String guessedWord) {
        if (guessedWord == null || guessedWord.length() != 4 || targetWord == null || targetWord.length() != 4) {
            LetterFeedback[] defaultFeedback = new LetterFeedback[4];
            char[] guessChars = guessedWord != null ? guessedWord.toCharArray() : new char[]{' ', ' ', ' ', ' '};
            for (int i = 0; i < 4; i++) {
                defaultFeedback[i] = new LetterFeedback(i < guessChars.length ? guessChars[i] : ' ', FeedbackState.NOT_IN_WORD);
            }
            return defaultFeedback;
        }
        LetterFeedback[] feedbackArray = new LetterFeedback[4];
        String target = targetWord.toLowerCase();
        String guess = guessedWord.toLowerCase();
        for (int i = 0; i < 4; i++) {
            char originalGuessedChar = guessedWord.charAt(i);
            char lowerGuessedChar = guess.charAt(i);
            FeedbackState state = (lowerGuessedChar == target.charAt(i)) ? FeedbackState.CORRECT_POSITION : FeedbackState.NOT_IN_WORD;
            feedbackArray[i] = new LetterFeedback(originalGuessedChar, state);
        }
        return feedbackArray;
    }

    public boolean makeMove(String word) {
        System.out.println("DEBUG makeMove: Attempting to make move with word: '" + (word == null ? "null" : word) + "'");
        if (word == null) {
            System.out.println("DEBUG makeMove: Input word is null. Returning false.");
            return false;
        }

        String lowerCaseWordInput = word.toLowerCase();
        String prevWord = gameHistory.isEmpty() ? startWord : gameHistory.get(gameHistory.size() - 1);

        if (prevWord == null) {
            System.err.println("ERROR makeMove: Previous word is null. Cannot make a move to '" + lowerCaseWordInput + "'.");
            if (showErrorMessage) {
                setChanged();
                notifyObservers("Error: Previous word not set!");
            }
            return false;
        }
        System.out.println("DEBUG makeMove: Previous word for move check is: '" + prevWord + "'");

        if (!isValidMove(prevWord, lowerCaseWordInput)) {
            System.out.println("DEBUG makeMove: Move from '" + prevWord + "' to '" + lowerCaseWordInput + "' is invalid. Returning false.");
            this.lastGuessFeedback = checkGuess(word);
            setChanged();
            if (showErrorMessage) {
                notifyObservers("invalid_move_feedback");
            } else {
                notifyObservers("invalid_move_no_error_feedback");
            }
            return false;
        }

        gameHistory.add(lowerCaseWordInput);
        this.lastGuessFeedback = checkGuess(word);
        System.out.println("DEBUG makeMove: Successfully moved to '" + lowerCaseWordInput + "'. History: " + gameHistory);
        setChanged();
        notifyObservers("move_successful_feedback");
        return true;
    }

    public boolean hasWon() {
        if (gameHistory.isEmpty()) return false;
        boolean won = gameHistory.get(gameHistory.size() - 1).equals(targetWord);
        return won;
    }

    public List<String> getDisplayableSolutionPath() {
        System.out.println("DEBUG Model.getDisplayableSolutionPath: Method called. isShowPath() = " + isShowPath());
        if (!isShowPath() || dictionary == null || dictionary.isEmpty()) {
            System.out.println("DEBUG Model.getDisplayableSolutionPath: showPath is false or dictionary invalid. Returning empty list.");
            return Collections.emptyList();
        }
        String actualStartWord = this.startWord;
        String actualTargetWord = this.targetWord;

        System.out.println("DEBUG Model.getDisplayableSolutionPath: Finding path from '" + actualStartWord + "' to '" + actualTargetWord + "'");
        if (actualStartWord == null || actualTargetWord == null || actualStartWord.isEmpty() || actualTargetWord.isEmpty()) {
            System.out.println("DEBUG Model.getDisplayableSolutionPath: Start or target word is null/empty. Returning empty list.");
            return Collections.emptyList();
        }

        actualStartWord = actualStartWord.toLowerCase();
        actualTargetWord = actualTargetWord.toLowerCase();

        if (actualStartWord.equals(actualTargetWord)) {
            System.out.println("DEBUG Model.getDisplayableSolutionPath: Start and target are same. Returning list with just start: [" + actualStartWord + "]");
            return Collections.singletonList(actualStartWord);
        }

        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        List<String> initialPath = new ArrayList<>();
        initialPath.add(actualStartWord);
        queue.add(initialPath);
        visited.add(actualStartWord);

        int pathsExplored = 0;
        while (!queue.isEmpty() && pathsExplored < 50000) {
            pathsExplored++;
            List<String> currentPath = queue.poll();
            String currentWord = currentPath.get(currentPath.size() - 1);
            if (currentWord.equals(actualTargetWord)) {
                System.out.println("DEBUG Model.getDisplayableSolutionPath: Path found: " + currentPath);
                return currentPath;
            }
            for (String neighbor : findNeighbors(currentWord)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(neighbor);
                    queue.add(newPath);
                }
            }
        }
        if (queue.isEmpty() && pathsExplored < 50000) {
            System.out.println("DEBUG Model.getDisplayableSolutionPath: BFS queue became empty. No path found after exploring " + pathsExplored + " options. Returning empty list.");
        } else if (pathsExplored >= 50000) {
            System.out.println("DEBUG Model.getDisplayableSolutionPath: Path exploration limit reached ("+ pathsExplored +"). No path found. Returning empty list.");
        }
        return Collections.emptyList();
    }

    private List<String> findNeighbors(String word) {
        List<String> neighbors = new ArrayList<>();
        char[] chars = word.toCharArray();
        for (int i = 0; i < word.length(); i++) {
            char originalChar = chars[i];
            for (char c = 'a'; c <= 'z'; c++) {
                if (c == originalChar) continue;
                chars[i] = c;
                String newWord = new String(chars);
                if (isValidWord(newWord)) {
                    neighbors.add(newWord);
                }
            }
            chars[i] = originalChar;
        }
        return neighbors;
    }

    public String getStartWord() { return startWord; }
    public String getTargetWord() { return targetWord; }
    public List<String> getGameHistory() { return new ArrayList<>(gameHistory); }
    public Model.LetterFeedback[] getLastGuessFeedback() { return lastGuessFeedback; }

    public void setShowErrorMessage(boolean show) {
        System.out.println("DEBUG Model.setShowErrorMessage: Called with show = " + show + ". Current this.showErrorMessage = " + this.showErrorMessage);
        if (this.showErrorMessage != show) {
            this.showErrorMessage = show;
            System.out.println("DEBUG Model.setShowErrorMessage: this.showErrorMessage is NOW " + this.showErrorMessage);
            setChanged();
            notifyObservers("flag_changed_showError_" + show);
        } else {
            System.out.println("DEBUG Model.setShowErrorMessage: Flag value did not change.");
        }
    }
    public void setShowPath(boolean show) {
        System.out.println("DEBUG Model.setShowPath: Called with show = " + show + ". Current this.showPath = " + this.showPath);
        if (this.showPath != show) {
            this.showPath = show;
            System.out.println("DEBUG Model.setShowPath: this.showPath is NOW " + this.showPath);
            setChanged();
            notifyObservers("flag_changed_showPath_" + show);
        } else {
            System.out.println("DEBUG Model.setShowPath: Flag value did not change.");
        }
    }
    public void setUseRandomWords(boolean use) {
        System.out.println("DEBUG Model.setUseRandomWords: Called with use = " + use + ". Current this.useRandomWords = " + this.useRandomWords);
        boolean valueChanged = (this.useRandomWords != use);
        this.useRandomWords = use;
        if (valueChanged || use) {
            System.out.println("DEBUG Model.setUseRandomWords: Value changed or use is true. Initializing game.");
            initializeGame();
        } else {
            System.out.println("DEBUG Model.setUseRandomWords: Value did not change and use is false. No re-initialization unless forced by other logic.");
        }
    }
    public boolean isShowErrorMessage() { return showErrorMessage; }
    public boolean isShowPath() { return showPath; }
    public boolean isUseRandomWords() { return useRandomWords; }

    public int getDictionarySize() {
        return this.dictionary != null ? this.dictionary.size() : 0;
    }
}
