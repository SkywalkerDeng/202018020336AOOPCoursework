import java.util.Observable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Model extends Observable {
    private String startWord;
    private String targetWord;
    private List<String> dictionary;
    private List<String> gameHistory;
    private boolean showErrorMessage;
    private boolean showPath;
    private boolean useRandomWords;
    
    public Model() {
        dictionary = loadDictionary("dictionary.txt");
        gameHistory = new ArrayList<>();
        showErrorMessage = true;
        showPath = false;
        useRandomWords = false;
        initializeGame();
    }
    
    private List<String> loadDictionary(String filename) {
        List<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 4) {  // Only load 4-letter words
                    words.add(line.toLowerCase());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        }
        return words;
    }
    // This is a comment for testing Git commit

    public void initializeGame() {
        if (useRandomWords) {
            Random rand = new Random();
            startWord = dictionary.get(rand.nextInt(dictionary.size()));
            do {
                targetWord = dictionary.get(rand.nextInt(dictionary.size()));
            } while (startWord.equals(targetWord));
        } else {
            startWord = "sale";  // Default starting word
            targetWord = "same"; // Default target word
        }
        gameHistory.clear();
        setChanged();
        notifyObservers();
    }
    
    public boolean isValidWord(String word) {
        return word.length() == 4 && dictionary.contains(word.toLowerCase());
    }
    
    public boolean isValidMove(String prevWord, String newWord) {
        if (!isValidWord(newWord)) return false;
        
        int differences = 0;
        for (int i = 0; i < 4; i++) {
            if (prevWord.charAt(i) != newWord.charAt(i)) {
                differences++;
            }
        }
        return differences == 1;
    }
    
    public boolean makeMove(String word) {
        word = word.toLowerCase();
        String prevWord = gameHistory.isEmpty() ? startWord : gameHistory.get(gameHistory.size() - 1);
        
        if (!isValidMove(prevWord, word)) {
            if (showErrorMessage) {
                setChanged();
                notifyObservers("Invalid move!");
            }
            return false;
        }
        
        gameHistory.add(word);
        setChanged();
        notifyObservers();
        return true;
    }
    
    public boolean hasWon() {
        return !gameHistory.isEmpty() && 
               gameHistory.get(gameHistory.size() - 1).equals(targetWord);
    }
    
    // Getters and setters
    public String getStartWord() { return startWord; }
    public String getTargetWord() { return targetWord; }
    public List<String> getGameHistory() { return new ArrayList<>(gameHistory); }
    public void setShowErrorMessage(boolean show) { 
        showErrorMessage = show;
        setChanged();
        notifyObservers();
    }
    public void setShowPath(boolean show) { 
        showPath = show;
        setChanged();
        notifyObservers();
    }
    public void setUseRandomWords(boolean use) { 
        useRandomWords = use;
        initializeGame();
    }
    public boolean isShowErrorMessage() { return showErrorMessage; }
    public boolean isShowPath() { return showPath; }
    public boolean isUseRandomWords() { return useRandomWords; }
} 