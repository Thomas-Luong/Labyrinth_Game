//I made it an abstract class bc interfaces can't have fields
public abstract class Agent {
    protected Tile tile;
    protected Board board;
    protected Game game;
    protected boolean isPlayer;

    public Tile getTile() {
        return this.tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public Board getBoard() {
        return this.board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isPlayer() {
        return this.isPlayer;
    }

    public void setIsPlayer(boolean isPlayer) {
        this.isPlayer = isPlayer;
    }

    Agent(Board board, Game game, Tile tile){
        this.board = board;
        this.game = game;
        this.tile = tile;
        tile.setAgent1(this);
    }
    
    //Move the agent around the board
    void move(Tile newTile){
        if (tile != null) {
            if(tile.getAgent2() != null){
                tile.setAgent1(tile.getAgent2());
                tile.setAgent2(null);
            }
            else{
                tile.setAgent1(null);
            }
        }
        
        newTile.setAgent1(this);
        tile = newTile;

    }

}
