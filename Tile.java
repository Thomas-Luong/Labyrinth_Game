public class Tile {
    private int x;
    private int y;
    private Agent agent1;
    private Agent agent2;
    public boolean visited;
    private boolean north;
    private boolean south;
    private boolean east;
    private boolean west;
    private boolean northVisible;
    private boolean southVisible;
    private boolean eastVisible;
    private boolean westVisible;
    
    public Tile(int x, int y){
        this.setX(x);
        this.setY(y);
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public boolean north() {
        return north;
    }

    public void setNorth(boolean north) {
        this.north = north;
    }

    public boolean south() {
        return south;
    }

    public void setSouth(boolean south) {
        this.south = south;
    }

    public boolean east() {
        return east;
    }

    public void setEast(boolean east) {
        this.east = east;
    }

    public boolean west() {
        return west;
    }

    public void setWest(boolean west) {
        this.west = west;
    }

    public boolean visited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean northVisible() {
        return northVisible;
    }

    public void setNorthVisible(boolean northVisible) {
        this.northVisible = northVisible;
    }

    public boolean southVisible() {
        return southVisible;
    }

    public void setSouthVisible(boolean southVisible) {
        this.southVisible = southVisible;
    }

    public boolean eastVisible() {
        return eastVisible;
    }

    public void setEastVisible(boolean eastVisible) {
        this.eastVisible = eastVisible;
    }

    public boolean westVisible() {
        return westVisible;
    }

    public void setWestVisible(boolean westVisible) {
        this.westVisible = westVisible;
    }


    public Agent getAgent1() {
        return agent1;
    }

    public void setAgent1(Agent agent) {
        this.agent1 = agent;

    }

    public boolean hasAgent(){
        return ((agent1 != null) || (agent2 != null));
    }

    public Agent getAgent2() {
        return this.agent2;
    }

    public void setAgent2(Agent agent2) {
        this.agent2 = agent2;
    }

}