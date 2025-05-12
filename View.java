import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class View extends JFrame implements Observer {
    private Model model;
    private Controller controller;

    // UI Components
    private JPanel mainPanel;
    private JPanel boardPanel;
    private JPanel solutionDisplayPanel;
    private JPanel keyboardPanel;
    private JButton resetButton;
    private JButton newGameButton;
    private JButton submitButton;
    private JTextField inputField;
    private JLabel messageLabel;
    private JToggleButton showErrorButton;
    private JToggleButton showPathButton;
    private JToggleButton randomWordsButton;

    // Color constants
    private static final Color CORRECT_COLOR = new Color(106, 170, 100);  // Green
    private static final Color WRONG_COLOR = new Color(120, 124, 126);    // Grey
    private static final Color SOLUTION_PATH_COLOR = new Color(204, 229, 255); // Light blue
    private static final Color DEFAULT_CELL_BG = Color.WHITE;
    private static final Color DEFAULT_CELL_FG = Color.BLACK;
    private static final Color START_TARGET_BG = Color.LIGHT_GRAY;


    private final int MAX_GUESS_ROWS = 6;
    private JLabel[][] guessLetterLabels;

    private final int MAX_SOLUTION_PATH_ROWS = 6;
    private JLabel[][] solutionPathLabels;

    private String currentArg = "";

    public View(Model model) {
        System.out.println("View Constructor: Initializing View with Model: " + model);
        this.model = model;
        initializeUI();
        this.model.addObserver(this);
        System.out.println("View Constructor: Observer added. Performing initial display update.");
        updateDisplayBasedOnModel();
    }

    public void setController(Controller controller) {
        System.out.println("View setController: Setting controller: " + controller);
        this.controller = controller;
        assignActionListeners();
    }

    private void initializeUI() {
        System.out.println("View initializeUI: Starting UI initialization.");
        setTitle("Weaver Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topControlsPanel = createControlsPanel();
        mainPanel.add(topControlsPanel, BorderLayout.NORTH);

        JPanel gameDisplayArea = new JPanel();
        gameDisplayArea.setLayout(new BoxLayout(gameDisplayArea, BoxLayout.Y_AXIS));

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(MAX_GUESS_ROWS + 2, 4, 3, 3));
        boardPanel.setBorder(BorderFactory.createTitledBorder("Your Guesses"));
        initializeBoardLabels();
        gameDisplayArea.add(boardPanel);

        solutionDisplayPanel = new JPanel();
        solutionDisplayPanel.setLayout(new GridLayout(MAX_SOLUTION_PATH_ROWS, 4, 3, 3));
        solutionDisplayPanel.setBorder(BorderFactory.createTitledBorder("Solution Path"));
        solutionDisplayPanel.setVisible(false); // Start hidden
        initializeSolutionPathLabels();
        gameDisplayArea.add(solutionDisplayPanel);

        JScrollPane scrollPane = new JScrollPane(gameDisplayArea);
        scrollPane.setPreferredSize(new Dimension(320, 450));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomAreaPanel = new JPanel(new BorderLayout(5, 5));
        JPanel inputControlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputField = new JTextField(8);
        inputField.setFont(new Font("Monospaced", Font.BOLD, 28));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.addActionListener(e -> submitWord());

        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.addActionListener(e -> submitWord());
        inputControlsPanel.add(inputField);
        inputControlsPanel.add(submitButton);
        bottomAreaPanel.add(inputControlsPanel, BorderLayout.NORTH);

        keyboardPanel = createKeyboard();
        bottomAreaPanel.add(keyboardPanel, BorderLayout.CENTER);

        messageLabel = new JLabel("Welcome to Weaver!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        bottomAreaPanel.add(messageLabel, BorderLayout.SOUTH);

        mainPanel.add(bottomAreaPanel, BorderLayout.SOUTH);
        add(mainPanel);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (inputField.hasFocus()) {
                if (e.getID() == KeyEvent.KEY_TYPED) {
                    char keyChar = e.getKeyChar();
                    if (Character.isLetter(keyChar)) {
                        String currentText = inputField.getText();
                        if (currentText.length() < 4) {
                            inputField.setText(currentText + Character.toLowerCase(keyChar));
                        }
                        return true;
                    } else if (keyChar == KeyEvent.VK_BACK_SPACE) {
                        String currentText = inputField.getText();
                        if (!currentText.isEmpty()) {
                            inputField.setText(currentText.substring(0, currentText.length() - 1));
                        }
                        return true;
                    }
                } else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitWord();
                    return true;
                }
            }
            return false;
        });

        pack();
        setMinimumSize(new Dimension(550, 700));
        setLocationRelativeTo(null);
        setVisible(true);
        System.out.println("View initializeUI: UI initialization complete. Frame visible.");
    }

    private void initializeBoardLabels() {
        System.out.println("View initializeBoardLabels: Initializing guessLetterLabels for boardPanel.");
        guessLetterLabels = new JLabel[MAX_GUESS_ROWS + 2][4];
        for (int i = 0; i < MAX_GUESS_ROWS + 2; i++) {
            for (int j = 0; j < 4; j++) {
                guessLetterLabels[i][j] = createSingleLetterLabel();
                boardPanel.add(guessLetterLabels[i][j]);
            }
        }
        System.out.println("View initializeBoardLabels: guessLetterLabels initialized and added to boardPanel.");
    }

    private void initializeSolutionPathLabels() {
        System.out.println("View initializeSolutionPathLabels: Initializing solutionPathLabels for solutionDisplayPanel.");
        solutionPathLabels = new JLabel[MAX_SOLUTION_PATH_ROWS][4];
        for (int i = 0; i < MAX_SOLUTION_PATH_ROWS; i++) {
            for (int j = 0; j < 4; j++) {
                solutionPathLabels[i][j] = createSingleLetterLabel();
                solutionDisplayPanel.add(solutionPathLabels[i][j]);
            }
        }
        System.out.println("View initializeSolutionPathLabels: solutionPathLabels initialized and added to solutionDisplayPanel.");
    }

    private JLabel createSingleLetterLabel() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("Monospaced", Font.BOLD, 32));
        label.setOpaque(true);
        label.setBackground(DEFAULT_CELL_BG);
        label.setForeground(DEFAULT_CELL_FG);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setPreferredSize(new Dimension(55, 55));
        return label;
    }

    private JPanel createControlsPanel() {
        System.out.println("View createControlsPanel: Creating top controls panel.");
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        resetButton = new JButton("Reset");
        newGameButton = new JButton("New Game");
        showErrorButton = new JToggleButton("Show Errors", model.isShowErrorMessage());
        showPathButton = new JToggleButton("Show Path", model.isShowPath());
        randomWordsButton = new JToggleButton("Random Words", model.isUseRandomWords());

        panel.add(resetButton);
        panel.add(newGameButton);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(showErrorButton);
        panel.add(showPathButton);
        panel.add(randomWordsButton);
        System.out.println("View createControlsPanel: Top controls panel created.");
        return panel;
    }

    private void assignActionListeners() {
        if (controller == null) {
            System.err.println("View Error: Controller not set. Action listeners cannot be assigned.");
            return;
        }
        System.out.println("View assignActionListeners: Assigning action listeners to control buttons.");
        resetButton.addActionListener(e -> controller.resetGame());
        newGameButton.addActionListener(e -> model.initializeGame());
        showErrorButton.addActionListener(e -> controller.toggleErrorMessages());
        showPathButton.addActionListener(e -> {
            System.out.println("View: Show Path button clicked. Current JToggleButton selected state: " + showPathButton.isSelected());
            controller.toggleShowPath();
        });
        randomWordsButton.addActionListener(e -> controller.toggleRandomWords());
    }


    private JPanel createKeyboard() {
        System.out.println("View createKeyboard: Creating keyboard panel.");
        JPanel keyboard = new JPanel(new GridBagLayout());
        String[][] keyRows = {
                {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
                {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
                {"ENTER", "Z", "X", "C", "V", "B", "N", "M", "DEL"}
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);

        for (int row = 0; row < keyRows.length; row++) {
            gbc.gridy = row;
            for (int col = 0; col < keyRows[row].length; col++) {
                String key = keyRows[row][col];
                JButton button = new JButton(key);
                button.setFont(new Font("Arial", Font.BOLD, 12));
                button.setMargin(new Insets(5, 5, 5, 5));

                gbc.gridx = col;
                gbc.gridwidth = 1;

                if (key.equals("ENTER")) {
                    button.addActionListener(e -> submitWord());
                } else if (key.equals("DEL")) {
                    button.addActionListener(e -> {
                        String currentText = inputField.getText();
                        if (!currentText.isEmpty()) {
                            inputField.setText(currentText.substring(0, currentText.length() - 1));
                        }
                    });
                } else {
                    button.addActionListener(e -> {
                        if (inputField.getText().length() < 4) {
                            inputField.setText(inputField.getText() + button.getText().toLowerCase());
                        }
                    });
                }
                if(key.equals("ENTER") || key.equals("DEL")) {
                    button.setPreferredSize(new Dimension(70, button.getPreferredSize().height));
                } else {
                    button.setPreferredSize(new Dimension(45, button.getPreferredSize().height));
                }
                keyboard.add(button, gbc);
            }
        }
        System.out.println("View createKeyboard: Keyboard panel created.");
        return keyboard;
    }

    private void submitWord() {
        String word = inputField.getText().trim();
        System.out.println("View submitWord: Attempting to submit word: '" + word + "'");
        if (controller != null && !word.isEmpty()) {
            controller.processInput(word);
            inputField.setText("");
            inputField.requestFocusInWindow();
            System.out.println("View submitWord: Word submitted and input field cleared.");
        } else if (controller == null) {
            System.err.println("View: Controller not set, cannot process input for word: '" + word + "'");
        } else {
            System.out.println("View submitWord: Word is empty, not submitting.");
        }
    }

    private void updateDisplayBasedOnModel() {
        System.out.println("View updateDisplayBasedOnModel: Updating display based on model state.");
        System.out.println("View updateDisplayBasedOnModel: StartWord='" + model.getStartWord() + "', TargetWord='" + model.getTargetWord() + "', HistorySize=" + model.getGameHistory().size());
        System.out.println("View updateDisplayBasedOnModel: isShowPath=" + model.isShowPath() + ", isShowError=" + model.isShowErrorMessage() + ", isRandom=" + model.isUseRandomWords());

        // 1. Update Start Word display
        String startWord = model.getStartWord();
        System.out.println("View updateDisplayBasedOnModel: Updating start word display for: '" + startWord + "'");
        updateWordRow(guessLetterLabels[0], startWord, false, null);

        // 2. Update Game History display
        List<String> history = model.getGameHistory();
        Model.LetterFeedback[] lastAttemptFeedback = model.getLastGuessFeedback();
        System.out.println("View updateDisplayBasedOnModel: History: " + history);
        System.out.println("View updateDisplayBasedOnModel: LastAttemptFeedback: " + (lastAttemptFeedback != null ? java.util.Arrays.toString(lastAttemptFeedback) : "null"));


        for (int i = 0; i < MAX_GUESS_ROWS; i++) {
            int historyRowIndex = i + 1;
            if (i < history.size()) {
                String guessedWord = history.get(i);
                System.out.println("View updateDisplayBasedOnModel: Updating history row " + i + " (JLabel row " + historyRowIndex + ") with word: '" + guessedWord + "'");
                Model.LetterFeedback[] feedback = model.checkGuess(guessedWord); // Get feedback for this historical guess
                System.out.println("View updateDisplayBasedOnModel: Feedback for '" + guessedWord + "': " + java.util.Arrays.toString(feedback));
                updateWordRow(guessLetterLabels[historyRowIndex], null, true, feedback);
            } else if (i == history.size() && lastAttemptFeedback != null &&
                    !"reset".equals(currentArg) && !currentArg.startsWith("flag_changed") &&
                    !"move_successful_feedback".equals(currentArg)) {
                // This block is to show feedback for an attempt that wasn't added to history (e.g. invalid move)
                System.out.println("View updateDisplayBasedOnModel: Displaying lastAttemptFeedback at history row " + i + " (JLabel row " + historyRowIndex + ")");
                updateWordRow(guessLetterLabels[historyRowIndex], null, true, lastAttemptFeedback);
            } else {
                // Clear unused guess rows
                System.out.println("View updateDisplayBasedOnModel: Clearing history row " + i + " (JLabel row " + historyRowIndex + ")");
                updateWordRow(guessLetterLabels[historyRowIndex], "", false, null);
            }
        }

        // 3. Update Target Word display
        String targetWord = model.getTargetWord();
        int targetRowDisplayIndex = MAX_GUESS_ROWS + 1;
        System.out.println("View updateDisplayBasedOnModel: Updating target word display for: '" + targetWord + "', HasWon=" + model.hasWon());
        updateWordRow(guessLetterLabels[targetRowDisplayIndex], targetWord, model.hasWon(), null);

        // 4. Update Solution Path Display
        System.out.println("View updateDisplayBasedOnModel: Checking showPath flag: " + model.isShowPath());
        // Get the direct parent of solutionDisplayPanel to revalidate it
        Container gameDisplayArea = solutionDisplayPanel.getParent();

        if (model.isShowPath()) {
            List<String> solutionPath = model.getDisplayableSolutionPath();
            System.out.println("View updateDisplayBasedOnModel: Show Path is TRUE. Solution Path from Model: " + solutionPath);
            boolean contentChanged = false; // To check if we actually update labels

            for (int i = 0; i < MAX_SOLUTION_PATH_ROWS; i++) {
                String currentTextInLabel = solutionPathLabels[i][0].getText(); // Check one label
                if (solutionPath != null && i < solutionPath.size()) {
                    String pathWord = solutionPath.get(i);
                    if (!pathWord.equalsIgnoreCase(currentTextInLabel)) contentChanged = true;
                    System.out.println("View updateDisplayBasedOnModel: Displaying solution path word " + i + ": '" + pathWord + "'");
                    updateWordRow(solutionPathLabels[i], pathWord, false, null, SOLUTION_PATH_COLOR, Color.BLACK);
                } else {
                    if (!currentTextInLabel.isEmpty()) contentChanged = true;
                    // System.out.println("View updateDisplayBasedOnModel: Clearing solution path row " + i);
                    updateWordRow(solutionPathLabels[i], "", false, null);
                }
            }
            // Only change visibility and revalidate if it was hidden or content changed
            if (!solutionDisplayPanel.isVisible() || contentChanged) {
                solutionDisplayPanel.setVisible(true);
                System.out.println("DEBUG View.updateDisplayBasedOnModel: solutionDisplayPanel set to VISIBLE or content changed.");
            }

        } else { // model.isShowPath() is false
            if (solutionDisplayPanel.isVisible()) { // Only hide if it was visible
                System.out.println("DEBUG View.updateDisplayBasedOnModel: Show Path is FALSE. Hiding solutionDisplayPanel.");
                for (int i = 0; i < MAX_SOLUTION_PATH_ROWS; i++) { // Clear labels before hiding
                    updateWordRow(solutionPathLabels[i], "", false, null);
                }
                solutionDisplayPanel.setVisible(false);
            }
        }
        // Revalidate the parent container of solutionDisplayPanel to reflect visibility changes
        if (gameDisplayArea != null) {
            gameDisplayArea.revalidate();
            gameDisplayArea.repaint();
        }


        // Update button states
        resetButton.setEnabled(!model.getGameHistory().isEmpty() && !model.hasWon());
        newGameButton.setEnabled(true);
        submitButton.setEnabled(!model.hasWon());
        inputField.setEnabled(!model.hasWon());

        showErrorButton.setSelected(model.isShowErrorMessage());
        showPathButton.setSelected(model.isShowPath());
        randomWordsButton.setSelected(model.isUseRandomWords());
        System.out.println("View updateDisplayBasedOnModel: Button states updated.");

        mainPanel.revalidate();
        mainPanel.repaint();
        System.out.println("View updateDisplayBasedOnModel: mainPanel revalidated and repainted. Update cycle finished.");
    }

    private void updateWordRow(JLabel[] rowLabels, String word, boolean applyFeedback, Model.LetterFeedback[] feedback, Color... customBgFg) {
        System.out.println("View updateWordRow: Updating a row. Word: '" + (word == null ? "null_str" : word) + "', ApplyFeedback: " + applyFeedback + ", Feedback: " + (feedback != null ? java.util.Arrays.toString(feedback) : "null_feedback") + ", CustomBG: " + (customBgFg != null && customBgFg.length > 0));
        Color defaultBg = (customBgFg != null && customBgFg.length > 0) ? customBgFg[0] : DEFAULT_CELL_BG;
        Color defaultFg = (customBgFg != null && customBgFg.length > 1) ? customBgFg[1] : DEFAULT_CELL_FG;

        for (int j = 0; j < 4; j++) {
            if (rowLabels == null || j >= rowLabels.length) {
                System.err.println("View updateWordRow: rowLabels is null or index out of bounds for j=" + j);
                continue;
            }

            JLabel cell = rowLabels[j];
            String textToSet = "";
            Color currentBg = defaultBg;
            Color currentFg = defaultFg;
            char letterToShow = ' ';

            if (applyFeedback && feedback != null && j < feedback.length) {
                letterToShow = feedback[j].letter;
                textToSet = String.valueOf(letterToShow).toUpperCase();
                if (feedback[j].state == Model.FeedbackState.CORRECT_POSITION) {
                    currentBg = CORRECT_COLOR;
                    currentFg = Color.WHITE;
                } else {
                    currentBg = WRONG_COLOR;
                    currentFg = Color.WHITE;
                }
            } else if (word != null && !word.isEmpty() && j < word.length()) {
                letterToShow = word.charAt(j);
                textToSet = String.valueOf(letterToShow).toUpperCase();
                if (customBgFg == null || customBgFg.length == 0) {
                    if (word.equals(model.getStartWord())) {
                        currentBg = START_TARGET_BG;
                        currentFg = Color.BLACK;
                    } else if (word.equals(model.getTargetWord())) {
                        if (model.hasWon()) {
                            currentBg = CORRECT_COLOR;
                            currentFg = Color.WHITE;
                        } else {
                            currentBg = START_TARGET_BG;
                            currentFg = Color.BLACK;
                        }
                    } else {
                        currentBg = DEFAULT_CELL_BG; // Should be default if not start/target and no custom BG
                        currentFg = DEFAULT_CELL_FG;
                    }
                }
                // If customBgFg is provided, currentBg/Fg are already set from defaultBg/Fg
            } else if (word != null && word.isEmpty() && feedback == null) {
                textToSet = "";
                if (customBgFg == null || customBgFg.length == 0) {
                    currentBg = DEFAULT_CELL_BG;
                    currentFg = DEFAULT_CELL_FG;
                }
            }

            System.out.println("View updateWordRow: Cell[" + j + "] Text='" + textToSet + "', BG=" + currentBg + ", FG=" + currentFg);
            cell.setText(textToSet);
            cell.setBackground(currentBg);
            cell.setForeground(currentFg);
        }
    }


    @Override
    public void update(Observable o, Object arg) {
        if (!(o instanceof Model)) return;
        SwingUtilities.invokeLater(() -> {
            this.currentArg = (arg instanceof String) ? (String) arg : "";
            System.out.println("View update (EDT): Received notification. Model changed. Arg: '" + this.currentArg + "'");

            updateDisplayBasedOnModel();

            String message = "";
            Color messageColor = Color.BLACK;

            switch (this.currentArg) {
                case "Error: Previous word not set!":
                    message = "Error: Game state issue, previous word not set.";
                    messageColor = Color.RED;
                    break;
                case "Invalid move!":
                    message = "Invalid move!";
                    messageColor = Color.RED;
                    break;
                case "invalid_move_feedback":
                    if (model.isShowErrorMessage()) {
                        message = "That move is not valid.";
                        messageColor = Color.RED;
                    }
                    break;
                case "reset":
                case "reset_error_dict":
                case "reset_error_dict_empty":
                    message = "Game has been reset. Good luck!";
                    inputField.requestFocusInWindow();
                    break;
                case "move_successful_feedback":
                    if (model.hasWon()) {
                        // Message will be handled by showWinMessage
                    } else {
                        message = "Move successful. Keep going!";
                    }
                    break;
                case "flag_changed_showError_true": message = "Error messages will now be shown."; break;
                case "flag_changed_showError_false": message = "Error messages will now be hidden."; break;
                case "flag_changed_showPath_true": message = "Solution path display enabled."; break;
                case "flag_changed_showPath_false": message = "Solution path display disabled."; break;
                default:
                    if (this.currentArg != null && !this.currentArg.isEmpty() &&
                            !this.currentArg.equals("flag_changed") &&
                            !this.currentArg.startsWith("flag_changed_") &&
                            !this.currentArg.equals("input_error_clear_feedback") &&
                            !this.currentArg.equals("invalid_move_no_error") &&
                            !this.currentArg.equals("invalid_move_no_error_feedback") ){
                        if(model.isShowErrorMessage()){
                            message = this.currentArg;
                            messageColor = Color.RED;
                        }
                    } else if (!model.hasWon() && !this.currentArg.startsWith("flag_changed")) {
                        message = " ";
                    }
                    break;
            }

            if (! (model.hasWon() && "move_successful_feedback".equals(this.currentArg)) ) {
                if (!message.trim().isEmpty()) {
                    messageLabel.setText("<html><font color='" + colorToHex(messageColor) + "'>" + message + "</font></html>");
                } else if (messageLabel.getText().startsWith("<html><font color='" + colorToHex(Color.RED))) {
                    messageLabel.setText(" ");
                }
            }

            if (model.hasWon() && "move_successful_feedback".equals(this.currentArg)) {
                showWinMessage();
            }
            System.out.println("View update (EDT): MessageLabel set to: '" + messageLabel.getText() + "'");
        });
    }

    private String colorToHex(Color color) {
        if (color == null) return "#000000";
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void showWinMessage() {
        System.out.println("View showWinMessage: Displaying win message dialog.");
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this,
                        "Congratulations! You've successfully transformed '" +
                                model.getStartWord() + "' into '" + model.getTargetWord() + "'!",
                        "Winner!",
                        JOptionPane.INFORMATION_MESSAGE)
        );
        messageLabel.setText("<html><font color='" + colorToHex(CORRECT_COLOR) + "'>You Won! Click 'New Game' to play again.</font></html>");
    }

    public void showError(String message) {
        System.out.println("View showError: Displaying error message: '" + message + "' in messageLabel.");
        SwingUtilities.invokeLater(() -> {
            messageLabel.setText("<html><font color='red'>" + message + "</font></html>");
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            System.out.println("View main: Creating Model, View, Controller.");
            Model model = new Model();
            View view = new View(model);
            Controller controller = new Controller(model, view);
            view.setController(controller);
            System.out.println("View main: Application started.");
        });
    }
}
