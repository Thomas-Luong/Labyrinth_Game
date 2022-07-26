public class Player extends Agent{
    private Tile secretRoom;
    private Treasure treasure;
    private boolean hasTreasure;
    private int totMovesPerTurn; // health
    private int curMovesPerTurn;
    private boolean dead;

    public boolean isDead() {
        return this.dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean getHasTreasure() {
        return this.hasTreasure;
    }

    public boolean inSecretRoom() {
        return getTile() == getSecretRoom();
    }

    public void setHasTreasure(boolean hasTreasure) {
        this.hasTreasure = hasTreasure;
    }

    public int getHealth() {
        if (totMovesPerTurn >= 8) {
            return 3;
        }
        if (totMovesPerTurn >= 6) {
            return 2;
        }
        if (totMovesPerTurn >= 4) {
            return 1;
        }
        return 0;
    }

    public int getTotMovesPerTurn() {
        return this.hasTreasure ? 4 : this.totMovesPerTurn;
    }

    public void setTotMovesPerTurn(int movesPerTurn) {
        this.totMovesPerTurn = movesPerTurn;
    }

    public int getCurMovesPerTurn() {
        return this.curMovesPerTurn;
    }

    public void setCurMovesPerTurn(int remainingTurn) {
        this.curMovesPerTurn = remainingTurn;
    }

    Player(Board board, Game game, Tile tile){
        super(board, game, tile);
        secretRoom = tile;
        totMovesPerTurn = 8;
        curMovesPerTurn = 8;
        isPlayer = true;
    }

    public Treasure getTreasure() {
        return this.treasure;
    }

    public void setTreasure(Treasure treasure) {
        this.treasure = treasure;
    }

    void resetMoves(){
        curMovesPerTurn = getTotMovesPerTurn();
    }

    void move(Tile newTile){
        super.move(newTile);
        curMovesPerTurn--;
    }

    Tile getSecretRoom() {
        return secretRoom;
    }

    void setSecretRoom(Tile secretRoom) {
        this.secretRoom = secretRoom;
    }

    //This is for when the foe attacks the player. The players health gets decrements.
    void receiveAttack() {
        game.showMessage("Player and Foe attack");
        game.playSound("FOE_ATTACKS");
        totMovesPerTurn -= 2;
        move(secretRoom);
        if (getHasTreasure()) {
            setHasTreasure(false);
            game.getTreasure().setPickedUp(false);
        }
        if(totMovesPerTurn == 6){
            game.showMessage("Player health now 6");
        }
        if(totMovesPerTurn == 4){
            game.showMessage("Player health now 4");
        }
        if(totMovesPerTurn == 2){
            totMovesPerTurn = 0;
            move(secretRoom);
        }
        curMovesPerTurn = 0;
    }
}
