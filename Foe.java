public class Foe extends Agent{
    boolean awake;
    boolean visible;
  
    
    public boolean isPlayerInRange(Player p) {
        // player can be null if it is a one player game so protect against that
        if(p == null) {
            return false;
        }

        // foe can't see the player if they are in their secret room
        if (p.inSecretRoom()) {
            return false;
        }

        if(Math.abs(tile.getX() - p.tile.getX()) <= 3 &&
           Math.abs(tile.getY() - p.tile.getY()) <= 3) {
            return true;
        }
        else {
            return false;
        }
    }

    public double getDistanceToPlayer(Player p) {
        // player can be null if it is a one player game so protect against that
        if(p == null) {
            return Double.MAX_VALUE;
        }
        return Math.sqrt(Math.pow(tile.getX() - p.tile.getX(),2) + 
                         Math.pow(tile.getY() - p.tile.getY(), 2));
    }
   

    public boolean isAwake() {
        return this.awake;
    }

    public void setAwake(boolean awake) {
        this.awake = awake;
    }

    public boolean isVisible() {
        return this.visible;
    }

    // set this to true when the foe attacks
    // set this to false when the foe stops chasing and returns to the treasure room
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
   
    Foe(Board board, Game game, Tile tile){
        super(board, game, tile);
        isPlayer = false;
    }

    //player awakes the foe, move for the first time
    void wake(){
        //chase(player);
    }

    //pursue the player 
    void chase(Tile nextTile){
        if (nextTile == tile) {
            return;
        }

        game.playSound("FOE_MOVES");

        int x = tile.getX();
        int y = tile.getY();
        // determine if the foe should move horizontal, vertical, or diagonal
        if(nextTile.getX() > tile.getX()) {
            x++;
        }
        else if(nextTile.getX() < tile.getX() ) {
            x--;
        }

        if(nextTile.getY() > tile.getY()) {
            y++;
        }
        else if(nextTile.getY() < tile.getY() ) {
            y--;
        }
        Tile newTile = board.getTile(x, y);
        // once we have determined where to move, but have not actually moved yet see if
        // there is already an agent on that tile and if there is, do the attack
        if(newTile != tile && newTile.getAgent1() != null) {
            // we know the agent has to be a player so cast it
            Player p = (Player)newTile.getAgent1();
            p.receiveAttack();
            setVisible(true);
        }
        move(newTile);
        
    }

}
