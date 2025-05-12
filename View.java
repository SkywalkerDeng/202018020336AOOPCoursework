import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;

public class View extends JFrame implements Observer {
    private Model model;
    private Controller controller;
    private JPanel mainPanel;
    private JPanel boardPanel;
    private JPanel keyboardPanel;
    private JButton resetButton;
    private JButton newGameButton;
    private JButton submitButton;
    private JTextField inputField;
    private JLabel messageLabel;
    private JToggleButton showErrorButton;
    private JToggleButton showPathButton;
    private JToggleButton randomWordsButton;
    
    private static final Color CORRECT_COLOR = new Color(106, 170, 100);  // 绿色
    private static final Color WRONG_POSITION_COLOR = new Color(201, 180, 88);  // 黄色
    private static final Color WRONG_COLOR = new Color(120, 124, 126);    // 灰色
    private static final Color KEYBOARD_BACKGROUND = Color.DARK_GRAY;     // 深灰色
    private static final Color KEYBOARD_TEXT = Color.WHITE;               // 白色
    
    public View(Model model) {
        this.model = model;
        initializeUI();
        model.addObserver(this);
    }
    
    public void setController(Controller controller) {
        this.controller = controller;
    }
    
    private void initializeUI() {
        setTitle("Weaver Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create game controls panel
        JPanel controlsPanel = createControlsPanel();
        mainPanel.add(controlsPanel, BorderLayout.NORTH);
        
        // Create game panel
        JPanel gamePanel = new JPanel(new BorderLayout(10, 10));
        
        // Create input panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputField = new JTextField(15);
        inputField.setFont(new Font("Arial", Font.PLAIN, 20));
        inputField.addActionListener(e -> submitWord());
        
        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.addActionListener(e -> submitWord());
        
        inputPanel.add(inputField);
        inputPanel.add(submitButton);
        gamePanel.add(inputPanel, BorderLayout.NORTH);
        
        // Create board panel
        boardPanel = new JPanel();
        boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.Y_AXIS));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(boardPanel);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        gamePanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        
        // Create keyboard panel
        keyboardPanel = createKeyboard();
        mainPanel.add(keyboardPanel, BorderLayout.SOUTH);
        
        // Create message label
        messageLabel = new JLabel(" ");
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(messageLabel, BorderLayout.EAST);
        
        add(mainPanel);
        
        // Add key listener for physical keyboard
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_TYPED && Character.isLetter(e.getKeyChar())) {
                String currentText = inputField.getText();
                if (currentText.length() < 4) {
                    inputField.setText(currentText + Character.toLowerCase(e.getKeyChar()));
                }
                return true;
            }
            return false;
        });
        
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 700));
    }
    
    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        // Create buttons
        resetButton = new JButton("Reset");
        resetButton.setEnabled(false);
        resetButton.addActionListener(e -> controller.resetGame());
        
        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> controller.resetGame());
        
        // Create toggle buttons for flags
        showErrorButton = new JToggleButton("Show Errors");
        showErrorButton.setSelected(true);
        showErrorButton.addActionListener(e -> controller.toggleErrorMessages());
        
        showPathButton = new JToggleButton("Show Path");
        showPathButton.addActionListener(e -> controller.toggleShowPath());
        
        randomWordsButton = new JToggleButton("Random Words");
        randomWordsButton.addActionListener(e -> controller.toggleRandomWords());
        
        panel.add(resetButton);
        panel.add(newGameButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(showErrorButton);
        panel.add(showPathButton);
        panel.add(randomWordsButton);
        
        return panel;
    }
    
    private JPanel createKeyboard() {
        JPanel keyboard = new JPanel(new GridBagLayout());
        keyboard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        keyboard.setBackground(new Color(211, 214, 218));  // 浅灰色背景
        
        String[][] keyRows = {
            {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"},
            {"A", "S", "D", "F", "G", "H", "J", "K", "L"},
            {"Z", "X", "C", "V", "B", "N", "M"}
        };
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);  // 增加按钮间距
        gbc.ipadx = 8;  // 增加按钮内部水平填充
        gbc.ipady = 8;  // 增加按钮内部垂直填充
        
        for (int row = 0; row < keyRows.length; row++) {
            gbc.gridy = row;
            
            // 计算每行的起始位置，使其居中
            int rowWidth = keyRows[row].length;
            int xOffset = (10 - rowWidth) / 2;  // 10是第一行的按钮数
            
            for (int col = 0; col < keyRows[row].length; col++) {
                gbc.gridx = col + xOffset;
                JButton button = new JButton(keyRows[row][col]);
                
                // 设置按钮样式
                button.setBackground(new Color(58, 58, 60));  // 深灰色背景
                button.setForeground(Color.WHITE);  // 白色文字
                button.setFont(new Font("Arial", Font.BOLD, 18));
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setOpaque(true);
                
                // 设置按钮大小
                button.setPreferredSize(new Dimension(50, 50));
                
                // 设置圆角边框
                button.putClientProperty("JButton.buttonType", "roundRect");
                
                // 添加鼠标悬停效果
                button.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        button.setBackground(new Color(78, 78, 80));
                    }
                    public void mouseExited(MouseEvent e) {
                        button.setBackground(new Color(58, 58, 60));
                    }
                });
                
                button.addActionListener(e -> {
                    if (inputField.getText().length() < 4) {
                        inputField.setText(inputField.getText() + 
                                        button.getText().toLowerCase());
                    }
                });
                
                keyboard.add(button, gbc);
            }
        }
        
        return keyboard;
    }
    
    private void submitWord() {
        String word = inputField.getText().trim();
        if (controller != null && !word.isEmpty()) {
            controller.processInput(word);
            inputField.setText("");
        }
    }
    
    private JLabel createWordLabel(String word, String targetWord) {
        JLabel label = new JLabel();
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        if (model.isShowPath() && targetWord != null) {
            StringBuilder html = new StringBuilder("<html><span style='letter-spacing: 5px'>");
            
            // 先标记已经完全匹配的字母
            boolean[] matched = new boolean[4];
            boolean[] used = new boolean[4];
            
            // 第一遍：找出位置完全匹配的字母
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == targetWord.charAt(i)) {
                    matched[i] = true;
                    used[i] = true;
                }
            }
            
            // 第二遍：处理存在但位置不对的字母
            for (int i = 0; i < word.length(); i++) {
                if (!matched[i]) {
                    // 检查这个字母是否在目标词中的其他位置
                    boolean found = false;
                    for (int j = 0; j < targetWord.length(); j++) {
                        if (!used[j] && word.charAt(i) == targetWord.charAt(j)) {
                            found = true;
                            used[j] = true;
                            break;
                        }
                    }
                    
                    String color;
                    if (matched[i]) {
                        color = String.format("#%02x%02x%02x", 
                            CORRECT_COLOR.getRed(),
                            CORRECT_COLOR.getGreen(),
                            CORRECT_COLOR.getBlue());
                    } else if (found) {
                        color = String.format("#%02x%02x%02x", 
                            WRONG_POSITION_COLOR.getRed(),
                            WRONG_POSITION_COLOR.getGreen(),
                            WRONG_POSITION_COLOR.getBlue());
                    } else {
                        color = String.format("#%02x%02x%02x",
                            WRONG_COLOR.getRed(),
                            WRONG_COLOR.getGreen(),
                            WRONG_COLOR.getBlue());
                    }
                    
                    html.append(String.format("<span style='color:%s'>%c</span>", 
                        color, word.charAt(i)));
                } else {
                    // 位置完全匹配的字母显示为绿色
                    html.append(String.format("<span style='color:#%02x%02x%02x'>%c</span>", 
                        CORRECT_COLOR.getRed(),
                        CORRECT_COLOR.getGreen(),
                        CORRECT_COLOR.getBlue(),
                        word.charAt(i)));
                }
            }
            
            html.append("</span></html>");
            label.setText(html.toString());
        } else {
            label.setText(word);
        }
        
        return label;
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if (!(o instanceof Model)) return;
        
        boardPanel.removeAll();
        
        // Add start word
        boardPanel.add(createWordLabel(model.getStartWord(), null));
        boardPanel.add(Box.createVerticalStrut(10));
        
        // Add history
        for (String word : model.getGameHistory()) {
            boardPanel.add(createWordLabel(word, model.getTargetWord()));
            boardPanel.add(Box.createVerticalStrut(10));
        }
        
        // Add target word
        boardPanel.add(createWordLabel(model.getTargetWord(), model.getTargetWord()));
        
        // Enable reset button if there's history
        resetButton.setEnabled(!model.getGameHistory().isEmpty());
        
        // Update toggle buttons
        showErrorButton.setSelected(model.isShowErrorMessage());
        showPathButton.setSelected(model.isShowPath());
        randomWordsButton.setSelected(model.isUseRandomWords());
        
        // Handle error messages
        if (arg instanceof String) {
            messageLabel.setText((String) arg);
            messageLabel.setForeground(Color.RED);
        } else {
            messageLabel.setText(" ");
        }
        
        revalidate();
        repaint();
    }
    
    public void showWinMessage() {
        JOptionPane.showMessageDialog(this,
            "Congratulations! You've successfully transformed '" + 
            model.getStartWord() + "' into '" + model.getTargetWord() + "'!",
            "Winner!",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setForeground(Color.RED);
        // 3秒后清除错误消息
        Timer timer = new Timer(3000, e -> {
            messageLabel.setText(" ");
            messageLabel.setForeground(Color.BLACK);
        });
        timer.setRepeats(false);
        timer.start();
    }
} 