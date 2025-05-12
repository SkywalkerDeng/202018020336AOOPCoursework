import java.util.Scanner;
// Ensure Model class is accessible.
// If Model is in a package (e.g., yourpackage.Model), you would need:
// import yourpackage.Model;

public class CLI {
    private Model model;
    private Scanner scanner;

    public CLI() {
        this.model = new Model();
        // REMOVE or COMMENT OUT the line below to use default words "sale" and "same"
        // this.model.setUseRandomWords(true);
        this.scanner = new Scanner(System.in);

        // Observer logic (can be kept as is or adjusted as needed)
        model.addObserver((o, arg) -> {
            String argument = (arg instanceof String) ? (String) arg : "";

            // CLI can update display or provide specific messages based on the argument
            // But the main game state display is controlled by the main loop to avoid duplicate printing.
            // Here we can handle specific notifications like game reset.

            if ("reset".equals(argument) || argument.startsWith("reset_error")) {
                System.out.println("\n--- Game has been reset/re-initialized ---");
                // displayGameState(); // Main loop will handle displaying state at the start of the next iteration
            } else if (argument.startsWith("flag_changed_")) {
                System.out.println("\n--- Settings changed: " + argument + " ---");
            }
            // Other types of 'arg' can be ignored as CLI's error/state handling is mainly in the main loop
        });
    }

    public void start() {
        System.out.println("Welcome to Weaver!");
        System.out.println("Transform one word into another by changing one letter at a time.");
        boolean gameRunning = true;

        // Model's constructor already calls initializeGame(), which will set up
        // default words if useRandomWords is false (which it is now by default here).

        while (gameRunning) {
            displayGameState(); // Display state at the beginning of each loop iteration.

            if (model.hasWon()) {
                System.out.println("Congratulations! You've won!");
                System.out.print("Play again? (y/n): ");
                if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                    // When playing again, Model's initializeGame will use the current state of useRandomWords.
                    // Since it's false, it will reset to default words "sale" and "same".
                    model.initializeGame();
                    continue;
                } else {
                    gameRunning = false;
                    break;
                }
            }

            System.out.print("Enter your word (or 'quit' to exit): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                gameRunning = false;
                break;
            }

            if (input.length() != 4) {
                if (model.isShowErrorMessage()) {
                    System.out.println("Error: Please enter a 4-letter word.");
                }
                continue;
            }

            boolean moveSuccessful = model.makeMove(input);

            if (!moveSuccessful) {
                if (model.isShowErrorMessage()) {
                    // Model's makeMove notifies observers with "invalid_move_feedback" on failure if showErrorMessage is true.
                    // The observer can handle this, or CLI can provide its own message here.
                    // To avoid relying on the observer printing internal messages unsuitable for CLI, CLI handles it directly.
                    System.out.println("Error: That move is not valid.");
                }
            }
            // Winning logic is handled at the beginning of the loop after displayGameState.
        }

        System.out.println("Thanks for playing Weaver!");
        scanner.close();
    }

    private void displayGameState() {
        System.out.println("\n------------------------------"); // Separator
        System.out.println("Start word:  " + model.getStartWord().toUpperCase());
        System.out.println("Target word: " + model.getTargetWord().toUpperCase());

        if (!model.getGameHistory().isEmpty()) {
            System.out.println("\nYour path so far:");
            int moveNumber = 1;
            for (String word : model.getGameHistory()) {
                System.out.println(moveNumber + ". " + word.toUpperCase());
                moveNumber++;
            }
        }
        System.out.println("------------------------------");
    }

    public static void main(String[] args) {
        new CLI().start();
    }
}
