public class Treasure {
    private Tile tile;
    private Player player;
    private boolean pickedUp;
    private boolean visible;

    public Tile getTile() {
        return this.tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public boolean isPickedUp() {
        return this.pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public boolean isVisible() {
        return this.visible;
    }

    // set this to true after the treasure room is found (so that the treasure is visible if the player holding it gets attacked)
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public Treasure(Tile tile) {
        this.tile = tile;
        this.pickedUp = false;
    }

    public void pickUp(Player player) {
        this.player = player;
        this.pickedUp = true;
    }

    public void drop() {
        pickedUp = false;
    }
}
