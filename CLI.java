import java.util.Scanner;

public class CLI {
    private Model model;
    private Scanner scanner;
    
    public CLI() {
        this.model = new Model();
        this.scanner = new Scanner(System.in);
        model.addObserver((o, arg) -> {
            if (arg instanceof String) {
                System.out.println("Error: " + arg);
            }
            displayGameState();
        });
    }
    
    public void start() {
        System.out.println("Welcome to Weaver!");
        System.out.println("Transform one word into another by changing one letter at a time.");
        
        while (true) {
            displayGameState();
            System.out.print("Enter your word (or 'quit' to exit): ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("quit")) {
                break;
            }
            
            if (input.length() != 4) {
                System.out.println("Please enter a 4-letter word");
                continue;
            }
            
            if (!model.isValidWord(input)) {
                System.out.println("Not a valid word");
                continue;
            }
            
            if (model.makeMove(input)) {
                if (model.hasWon()) {
                    System.out.println("Congratulations! You've won!");
                    System.out.print("Play again? (y/n): ");
                    if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                        model.initializeGame();
                    } else {
                        break;
                    }
                }
            }
        }
        
        scanner.close();
    }
    
    private void displayGameState() {
        System.out.println("\nStart word: " + model.getStartWord());
        System.out.println("Target word: " + model.getTargetWord());
        
        if (!model.getGameHistory().isEmpty()) {
            System.out.println("\nYour moves:");
            for (String word : model.getGameHistory()) {
                System.out.println(word);
            }
        }
        System.out.println();
    }
    
    public static void main(String[] args) {
        new CLI().start();
    }
} 