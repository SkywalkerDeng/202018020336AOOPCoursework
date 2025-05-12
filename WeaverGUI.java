public class WeaverGUI {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Model model = new Model();
            View view = new View(model);
            Controller controller = new Controller(model, view);
            view.setController(controller);
            view.setVisible(true);
        });
    }
} 