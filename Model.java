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
    private List<String> dictionary; // 您的私有字典字段
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
            // 确保字典不为空，以避免 rand.nextInt() 出错
            if (dictionary == null || dictionary.isEmpty()) {
                System.err.println("Dictionary is empty or not loaded. Cannot set random words.");
                // 可以选择设置默认词或者抛出异常
                startWord = "error"; // 或者其他合适的错误指示词
                targetWord = "error";
                gameHistory.clear();
                setChanged();
                notifyObservers();
                return;
            }
            Random rand = new Random();
            startWord = dictionary.get(rand.nextInt(dictionary.size()));
            do {
                targetWord = dictionary.get(rand.nextInt(dictionary.size()));
            } while (startWord.equals(targetWord) && dictionary.size() > 1); // 添加 dictionary.size() > 1 条件避免无限循环
        } else {
            startWord = "sale";  // Default starting word
            targetWord = "same"; // Default target word
        }
        gameHistory.clear();
        setChanged();
        notifyObservers();
    }

    public boolean isValidWord(String word) {
        if (word == null || dictionary == null) { // 添加对 dictionary 的 null 检查
            return false;
        }
        return word.length() == 4 && dictionary.contains(word.toLowerCase());
    }

    public boolean isValidMove(String prevWord, String newWord) {
        if (prevWord == null || newWord == null) { // 添加对参数的 null 检查
            return false;
        }
        if (!isValidWord(newWord)) return false;

        // 确保 prevWord 也是4个字符，虽然通常它来自 startWord 或 history
        if (prevWord.length() != 4) return false;

        int differences = 0;
        for (int i = 0; i < 4; i++) {
            if (prevWord.charAt(i) != newWord.charAt(i)) {
                differences++;
            }
        }
        return differences == 1;
    }

    public boolean makeMove(String word) {
        if (word == null) return false; // 添加对 word 的 null 检查
        word = word.toLowerCase();
        String prevWord = gameHistory.isEmpty() ? startWord : gameHistory.get(gameHistory.size() - 1);

        // 确保 prevWord 不是 null，尤其是在 startWord 可能因为字典加载失败而未正确初始化时
        if (prevWord == null) {
            if (showErrorMessage) {
                System.err.println("Error: Previous word is not set, cannot make a move.");
                setChanged(); // 也可以考虑通知观察者这个错误状态
                notifyObservers("Error: Previous word not set!");
            }
            return false;
        }

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
    public List<String> getGameHistory() { return new ArrayList<>(gameHistory); } // 返回副本是个好习惯
    public void setShowErrorMessage(boolean show) {
        showErrorMessage = show;
        setChanged(); // 仅在状态实际改变时调用 setChanged() 通常更好，但这里保持原样
        notifyObservers();
    }
    public void setShowPath(boolean show) {
        showPath = show;
        setChanged();
        notifyObservers();
    }
    public void setUseRandomWords(boolean use) {
        // 只有当 useRandomWords 的状态实际改变时才重新初始化游戏，可能更高效
        // if (this.useRandomWords != use) {
        //    this.useRandomWords = use;
        //    initializeGame();
        // }
        // 但为了与测试预期一致（setUseRandomWords 后立即获取新词），保持原样
        this.useRandomWords = use;
        initializeGame(); // initializeGame 会调用 setChanged 和 notifyObservers
    }
    public boolean isShowErrorMessage() { return showErrorMessage; }
    public boolean isShowPath() { return showPath; }
    public boolean isUseRandomWords() { return useRandomWords; }

    // *** 添加这个公共方法 ***
    public int getDictionarySize() {
        return this.dictionary != null ? this.dictionary.size() : 0;
    }
}