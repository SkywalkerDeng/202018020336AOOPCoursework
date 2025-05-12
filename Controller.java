public class Controller {
    private Model model;
    private View view;
    
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }
    
    public void processInput(String input) {
        if (input == null || input.length() != 4) {
            if (model.isShowErrorMessage()) {
                view.showError("Please enter a 4-letter word");
            }
            return;
        }
        
        input = input.toLowerCase();
        if (!model.isValidWord(input)) {
            if (model.isShowErrorMessage()) {
                view.showError("Not a valid word");
            }
            return;
        }
        
        String prevWord = model.getGameHistory().isEmpty() ? model.getStartWord() : 
                         model.getGameHistory().get(model.getGameHistory().size() - 1);
        
        if (!model.isValidMove(prevWord, input)) {
            if (model.isShowErrorMessage()) {
                view.showError("You can only change one letter at a time");
            }
            return;
        }
        
        if (model.makeMove(input)) {
            if (model.hasWon()) {
                view.showWinMessage();
            }
        }
    }
    
    public void resetGame() {
        model.initializeGame();
    }
    
    public void toggleErrorMessages() {
        model.setShowErrorMessage(!model.isShowErrorMessage());
    }
    
    public void toggleShowPath() {
        model.setShowPath(!model.isShowPath());
    }
    
    public void toggleRandomWords() {
        model.setUseRandomWords(!model.isUseRandomWords());
    }
} 