public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        Game game = new Game();
        GameController controller = new GameController(game);
        CustomGameGraphics cgg = new CustomGameGraphics(controller);   
        controller.setGraphics(cgg);
    }
}
