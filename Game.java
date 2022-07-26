import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Game implements Subject {
    private Player player1;
    private Player player2;
    private Foe foe;
    private Board board;
    private boolean started;
    private int activePlayer;
    private Treasure treasure;
    private GameState state;

    public enum GameState {
        P1_TURN,
        P2_TURN,
        FOE_TURN,
        GAME_OVER
    }

    public Treasure getTreasure() {
        return this.treasure;
    }

    public void setTreasure(Treasure treasure) {
        this.treasure = treasure;
    }

    // Observers are notified for model-independent events/updates
    // e.g. sound effects, informational messages, etc.
    private List<Observer> observers;

    // Constructor
    public Game() {
        board = new Board(8, 8);
        observers = new java.util.ArrayList<Observer>();
        reset();
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Foe getFoe() {
        return foe;
    }

    public Board getBoard() {
        return board;
    }

    public void selectTile(int column, int row) {
        Tile requestedTile = board.getTile(column, row);
        selectTile(requestedTile);
    }

    // Player selects a tile to move to
    public void selectTile(Tile requestedTile) {
        if(state == GameState.FOE_TURN) {
            return;
        }

        if (state == GameState.GAME_OVER) {
            return;
        }

        Player curPlayer = getCurPlayer();
        if (!started) {
            selectSecretRoom(requestedTile);
        } else if (curPlayer.getCurMovesPerTurn() > 0) {
            Tile curTile = curPlayer.getTile();
            if(isAdjacent(requestedTile, curTile)) {
                if (isWalled(curPlayer.getTile(), requestedTile, true)) {
                    hitWall();
                    return;
                }
                processTurn(curPlayer, requestedTile);
            } else {
                invalidMove();
            }
        } else {
            playSound("ILLEGAL_MOVE");
            showMessage("P" + activePlayer + ": No moves left!");
        }
    }

    //checks if two tiles are adjacent to each other
    private boolean isAdjacent(Tile t1, Tile t2) {
        int x1 = t1.getX();
        int y1 = t1.getY();
        int x2 = t2.getX();
        int y2 = t2.getY();
        
        return x1 == x2 && Math.abs(y1 - y2) == 1 ||
               y1 == y2 && Math.abs(x1 - x2) == 1;
    }

    // Reset the game to its initial state
    public void reset() {
        playSound("ON");
        player1 = null;
        player2 = null;
        foe = null;
        started = false;
        activePlayer = 1;
        state = GameState.P1_TURN;
        board.generateMaze();
    }

    //Pick random tile for a player to land on if they end their turn on another player
    void checkLandingCollision(Player player, Tile newTile){
        if(newTile.getAgent2() != null){
            Tile randTile = getRandomAdjacentTile(newTile);
            while(isWalled(newTile, randTile)){
                randTile = getRandomAdjacentTile(newTile);
            }
            player.move(randTile);
        }
    }

    // Handle next based on the current state of the game
    public void next() {
        if (state == GameState.GAME_OVER) {
            playSound("ILLEGAL_MOVE");
            showMessage("The game is over!");
            return;
        }
        playSound("ON");
        if (started) {
            Player curPlayer = getCurPlayer();
            checkLandingCollision(curPlayer, curPlayer.getTile());
            endTurn();
        } else {
            processNextSecretRoom();
        }
    }

    // End the current turn
    public void endTurn() {
        if(player2 == null){
            if(player1.getHealth() <= 0 && player1.isDead() == false){
                player1.setDead(true);
                showMessage("You lose!");
                playSound("DEFEAT");
                state = GameState.GAME_OVER;
            }
        }else {
            if(player1.getHealth() <= 0 && player1.isDead() == false){
                showMessage("Player 1 loses!");
                playSound("DEFEAT");
                player1.setDead(true);
                player1.move(dummyTile());
            }
            else if(player2.getHealth() <= 0 && player2.isDead() == false){
                showMessage("Player 2 loses!");
                playSound("DEFEAT");
                player2.setDead(true);
                player2.move(dummyTile());
            }

            if (player1.isDead() && player2.isDead()) {
                showMessage("Both players lose!");
                state = GameState.GAME_OVER;
            }
        }
        player1.resetMoves();
        if(player2 != null){
            player2.resetMoves();
        }
        nextState();
        if (state == GameState.FOE_TURN) {
            processFoeTurn();
        }
    }

    // Returns an unused tile that is off the board
    private Tile dummyTile(){
        return new Tile(-999, -999);
    }

    //Handles the Foe's automation on it's turn
    private void processFoeTurn() {
        if(foe.isAwake()) {
            // when it is the foe's turn he should chase the player with the treasure
            // uncondonditionally or the closest player in range
            if(player1.getHasTreasure()) {
                showMessage("Foe chases player 1");
                foe.chase(player1.getTile());
            }
            else if(player2 != null && player2.getHasTreasure()) {
                showMessage("Foe chases player 2");
                foe.chase(player2.getTile());
            }
            else if(foe.isPlayerInRange(player1) && !foe.isPlayerInRange(player2) && !player1.inSecretRoom()) {
                showMessage("Foe chases player 1");
                foe.chase(player1.getTile());
            }
            else if(!foe.isPlayerInRange(player1) && foe.isPlayerInRange(player2) && !player2.inSecretRoom()) {
                showMessage("Foe chases player 2");
                foe.chase(player2.getTile());
            }
            else if(foe.isPlayerInRange(player1) && foe.isPlayerInRange(player2) && !player1.inSecretRoom() && !player2.inSecretRoom()) {
                if(foe.getDistanceToPlayer(player1) < foe.getDistanceToPlayer(player2)) {
                    showMessage("Foe chases player 1");
                    foe.chase(player1.getTile());
                }
                else {
                    showMessage("Foe chases player 2");
                    foe.chase(player2.getTile());
                }
            }
            else {
                foe.setVisible(false);
                if (foe.getTile() == treasure.getTile()) {
                    showMessage("Foe guards treasure");
                } else {
                    showMessage("Foe moves towards treasure");
                    foe.chase(treasure.getTile());
                }
            }
        }
        endTurn();
    }

    //Handles the selection of a secret room
    public void processNextSecretRoom() {
        if (player1 == null) {
            showMessage("Please select a tile.");
        } else {
            if (activePlayer == 2) {
                if (!is1Player()) {
                    showMessage("P2 secret room selected.");
                }
                activePlayer = 1;
                start();
            } else {
                showMessage("P1 secret room selected.");
                activePlayer = 2;
            }
        }
    }

    //Creates and places the foe
    public void setFoe(){
        int x1 = player1.getSecretRoom().getX();
        int x2 = is1Player() ? -2 : player2.getSecretRoom().getX();
        int y1 = player1.getSecretRoom().getY();
        int y2 = is1Player() ? -2 : player2.getSecretRoom().getY();
        //generate foe location
        int[] foeLoc = getRandLocation(x1,x2,y1,y2);
        foe = new Foe(board, this, board.getTile(foeLoc[0], foeLoc[1]));
    }

    //Gets a random location on the board and returns it as an x y coordinate
    private int[] getRandLocation(int x1, int x2, int y1, int y2){
        int[] loc = new int[2];
        loc[0] = x1;
        loc[1] = y1;

        while (distance(x1, loc[0], y1, loc[1]) < 3 ||
               distance(x2, loc[0], y2, loc[1]) < 3) {
            loc[0] = getRandomNumber(0, board.getWidth());
            loc[1] = getRandomNumber(0, board.getHeight());
        }
        return loc;
    }

    //calculates distance
    private double distance(int x1, int x2, int y1, int y2) {
        // manhattan distance:
        //return Math.abs(x1 - x2) + Math.abs(y1 - y2);
        // euclidean distance:
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    //Creates and places the treasure
    public void setTreasure(){
        Tile tresTile = foe.getTile();
        treasure = new Treasure(tresTile);
        player1.setTreasure(treasure);
        if (!is1Player()) {
            player2.setTreasure(treasure);
        }
    }

    public int getActivePlayer() {
        return activePlayer;
    }

    // Set secret room for the active player.
    private void selectSecretRoom(Tile requestedTile) {
        if (requestedTile.hasAgent()) {
            playSound("ILLEGAL_MOVE");
            showMessage("That tile is taken.");
            return;
        }

        playSound("PLAYER_MOVE");

        if (activePlayer == 1) {
            if (player1 == null) {
                player1 = new Player(board, this, requestedTile);
            } else {
                player1.move(requestedTile);
                player1.resetMoves();
            }

            player1.setSecretRoom(requestedTile);
        } else {
            if (player2 == null) {
                player2 = new Player(board, this, requestedTile);
            } else {
                player2.move(requestedTile);
                player2.resetMoves();
            }

            player2.setSecretRoom(requestedTile);
        }
    }

    // Start the game
    private void start() {
        started = true;
        if (is1Player()) {
            showMessage("1-player game started.");
        } else {
            showMessage("2-player game started.");
        }
        setFoe();
        setTreasure();
        showMessage("Player 1's turn (" + player1.getCurMovesPerTurn() + " moves)");
    }

    //Handles the changing of turns
    public void processTurn(Player player, Tile requestedTile) {
        boolean foeThere = checkFoe(player, requestedTile);
        if(foeThere == true){
            player.receiveAttack();
            foe.setVisible(true);
            endTurn();
        }
        else{
            boolean collision = checkPVPCollision(player, requestedTile);
            if(collision == false){
                player.move(requestedTile);
                playSound("PLAYER_MOVE");
                checkTreasure(requestedTile);
                if (state != GameState.GAME_OVER) {
                    showMovesLeft();
                }
            }
            else{
                handlePVP(player, requestedTile);
            }
        }

        // check if foe should wake
        if (!foe.isAwake() &&
            (player1 != null && !player1.isDead() && foe.getDistanceToPlayer(player1) <= 3 ||
             player2 != null && !player2.isDead() && foe.getDistanceToPlayer(player2) <= 3)) {
            foe.setAwake(true);
            showMessage("The foe awakes!");
        }
    }

    //checks if the foe is on newtile
    private boolean checkFoe(Player player, Tile newTile){
        Agent other = newTile.getAgent1();
        try{
            Foe foe = (Foe)other;
            if(foe != null){
                return true;
            }
            else{
                return false;
            }
        }
        catch(ClassCastException e){
            return false;
        }
    }

    //displays moves a player has available
    private void showMovesLeft() {
        Player player = getCurPlayer();
        if (player.getCurMovesPerTurn() == 1) {
            showMessage("P" + activePlayer + ": 1 move left");
        } else {
            showMessage("P" + activePlayer + ": " + player.getCurMovesPerTurn() + " moves left");
        }
    }

    //checks if a player wins or collects the treasure
    private void checkTreasure(Tile requestedTile) {
        Player player = getCurPlayer();
        if (player.getHasTreasure() && player.inSecretRoom()) {
            win();
        } else if (!treasure.isPickedUp() && requestedTile == treasure.getTile()) {
            treasure.setPickedUp(true);
            treasure.setVisible(true);
            player.setHasTreasure(true);
            player.setCurMovesPerTurn(Math.min(4, player.getCurMovesPerTurn()));
            playSound("GOAL");
            showMessage("P" + activePlayer + " found the treasure!");
        }
    }

    //Displays winning and ends the game
    public void win() {
        showMessage("Player " + activePlayer + " wins!!");
        playSound("WINNER");
        state = GameState.GAME_OVER;
    }

    //Checks if a player collision should initiate PVP
    public boolean checkPVPCollision(Player player, Tile newTile) {
        Agent other = newTile.getAgent1();
        //remember to update the view
        if(other != null && other.isPlayer()){
            return true;
        }
        return false;
    }

    //Checks if a player is landing in their secret room
    public boolean checkSecretRoomCollision(Player player, Tile newTile){
        if(newTile == player.getSecretRoom()){
            return true;
        }
        return false;
    }

    //Manages PVP
    public void handlePVP(Player player, Tile newTile){
        Agent other = newTile.getAgent1();
        Player otherPlayer = (Player)other;
        //otherplayer not null
        boolean myRoom = checkSecretRoomCollision(player, newTile);
        boolean theirRoom = checkSecretRoomCollision(player, newTile);
        if(otherPlayer.getHasTreasure() == true || player.getHasTreasure() == true){ //PVP
            System.out.println("here");
            playSound("ILLEGAL_MOVE");
            showMessage("PVP!");
            Tile randTile = loseCombatTile(newTile);
            if(myRoom == false && (player.getHealth() > otherPlayer.getHealth() && (theirRoom == false))){
                player.setHasTreasure(true);
                showMessage("You win combat and get the treasure!");
                otherPlayer.setHasTreasure(false);
                otherPlayer.move(randTile);
                player.move(newTile);
                player.setCurMovesPerTurn(Math.min(4, player.getCurMovesPerTurn()));
            }
            else{
                showMessage("You lose combat!");
                otherPlayer.setHasTreasure(true);
                player.setHasTreasure(false);
                otherPlayer.setCurMovesPerTurn(Math.min(4, otherPlayer.getCurMovesPerTurn()));
                player.move(randTile);
                player.setCurMovesPerTurn(0);
            }
        }
        else{
            //stack the players
            newTile.setAgent2(otherPlayer);
            player.move(newTile);
            playSound("PLAYER_MOVE");
        }
    }

    //Ends a players turn when they hit a wall
    public void hitWall(){
        playSound("WALL");
        showMessage("P" + activePlayer + ": Your turn is over.");
        showMessage("P" + activePlayer + ": You hit a wall!");
        endTurn();
    }

    private void invalidMove() {
        playSound("ILLEGAL_MOVE");
        showMessage("Invalid move for P" + activePlayer);
    }

    private boolean isWalled(Tile source, Tile dest) {
        return isWalled(source, dest, false);
    }

    //Displays walls on a tile
    private boolean isWalled(Tile source, Tile dest, boolean makeVisible) {
        int sx = source.getX();
        int sy = source.getY();
        int dx = dest.getX();
        int dy = dest.getY();

        if (sx > dx) {
            // moving left
            if (source.west()) {
                if (makeVisible) {
                    source.setWestVisible(true);
                }
                return true;
            }
        } else if (sx < dx) {
            // moving right
            if (source.east()) {
                if (makeVisible) {
                    source.setEastVisible(true);
                }
                return true;
            }
        } else if (sy > dy) {
            // moving up
            if (source.north()) {
                if (makeVisible) {
                    source.setNorthVisible(true);
                }
                return true;
            }
        } else if (sy < dy) {
            // moving down
            if (source.south()) {
                if (makeVisible) {
                    source.setSouthVisible(true);
                }
                return true;
            }
        }
        return false;
    }
    
    //Gets a random tile which is adjacent to curTile
    private Tile getRandomAdjacentTile(Tile curTile){
        int randX = 0;
        int randY = 0;
        int newX = 0;
        int newY = 0;
        while(!(randX == 0 ^ randY == 0) || newX < 0 || newX >= board.getWidth() || newY < 0 || newY >= board.getHeight()){
            randX = ThreadLocalRandom.current().nextInt(-1, 2);
            randY = ThreadLocalRandom.current().nextInt(-1, 2);
            newX = curTile.getX() + randX;
            newY = curTile.getY() + randY;
        }
        Tile newTile = board.getTile(newX, newY);
        return newTile;
    }

    //selects the direction a player will move in when losing combat
    private Tile loseCombatTile(Tile curTile){
        Tile finalTile = board.getTile(0,0);
        int[] newCoords = {-1,-1};
        boolean placePicked = false;
        int direction = -1;
        int nextDir = -1;
        while(placePicked == false){
            direction = ThreadLocalRandom.current().nextInt(0, 4);
            nextDir = ThreadLocalRandom.current().nextInt(-3, 4);
            switch(direction){ //TODO: make sure they don't land on the foe
                case 0: //up
                    newCoords[0] = nextDir;
                    newCoords[1] = curTile.getY()+3;
                    break;
                case 1://down
                    newCoords[0] = nextDir;
                    newCoords[1] = curTile.getY()-3;
                    break;
                case 2://left
                    newCoords[0] = curTile.getX()-3;
                    newCoords[1] = nextDir;
                    break;
                case 3://right
                    newCoords[0] = curTile.getX()+3;
                    newCoords[1] = nextDir;
                    break;
            }
            if(newCoords[0] >= 0 && newCoords[0] <= 7 && newCoords[1] >= 0 && newCoords[1] <= 7){
                placePicked = true;
                finalTile = board.getTile(newCoords[0], newCoords[1]);
            }
        }
        //should never get here
        return finalTile;
    }

    // set active player to 1 or 2 and also advance state
    // depending on if it is a 1 player game P1_TURN -> FOE_TURN -> P1_turn...)
    // if it is a 2 player game P1_TURN -> P2_Turn -> FOE_TURN -> P1_TURN ...
    private void nextState() {
        if(state == GameState.P1_TURN){
            if(is1Player() || player2.isDead() && player1.isDead() == false) {
                state = GameState.FOE_TURN;
                activePlayer = 1;
                showMessage("Foe's turn");
            }
            else {
                state = GameState.P2_TURN;
                activePlayer = 2;
                //player2.resetMoves();
                showMessage("Player 2's turn (" + player2.getCurMovesPerTurn() + " moves)");
            }
        }
        else if(state == GameState.P2_TURN) {
            state = GameState.FOE_TURN;
            showMessage("Foe's turn");
        }
        else if(player1.getHealth() != 0 || state == GameState.FOE_TURN){
            if(player1.getHealth() == 0){
                showMessage("Player 2's turn (" + player2.getCurMovesPerTurn() + " moves)");
                state = GameState.P2_TURN;
            }else{
                state = GameState.P1_TURN;
                activePlayer = 1;
                //player1.resetMoves();
                showMessage("Player 1's turn (" + player1.getCurMovesPerTurn() + " moves)");
            }
        }
    }

    // Return the active player
    private Player getCurPlayer() {
        if (activePlayer == 1) {
            return player1;
        } else {
            return player2;
        }
    }

    // Returns true if the game is 1-player
    private boolean is1Player() {
        return player2 == null;
    }

    // Notify observers of a message to be displayed
    public void showMessage(String message) {
        GameEvent event = new GameEvent(GameEvent.EventType.SHOW_MESSAGE, message);
        notifyObservers(event);
    }

    // Notify observers of a sound effect to be played
    public void playSound(String soundName) {
        GameEvent event = new GameEvent(GameEvent.EventType.PLAY_SOUND, soundName);
        notifyObservers(event);
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(Object arg) {
        for (Observer o : observers) {
            o.update(arg);
        }
    }
}
