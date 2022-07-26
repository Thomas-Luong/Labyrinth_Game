//import StdRandom;
//import java.util.Collections;
//import java.util.Arrays;

public class Board {
    private Tile[][] tiles;
    private Tile secretRoom1;
    private Tile secretRoom2;
    private int width;
    private int height;

    private static final double DEGRADE_FACTOR = 0.25;

    public Tile getSecretRoom1() {
        return this.secretRoom1;
    }

    public void setSecretRoom1(Tile secretRoom1) {
        this.secretRoom1 = secretRoom1;
    }

    public Tile getSecretRoom2() {
        return this.secretRoom2;
    }

    public void setSecretRoom2(Tile secretRoom2) {
        this.secretRoom2 = secretRoom2;
    }

    //will have tiles in place when done
    public void generateMaze()
    {

        //Generating the board////////////////////////
        Maze mm = new Maze(8,8);

        int[][] maze = mm.getMaze(); //get the maze in the form maze has

        degradeWalls(maze); // randomly delete walls

        tiles = new Tile[maze.length][maze[0].length]; //make a container

        //convert the maze to a tile with walls n, s, e, w
        for (int i = 0; i < maze[0].length; i++)
        {
            for (int j = 0; j < maze.length; j++)
            {
                Tile tile = new Tile(j,i);

                tile.setWest((maze[j][i] & 8) == 0);
                tile.setNorth((maze[j][i] & 1) == 0);

                if (j < maze.length - 1)
                {
                    tile.setEast((maze[j + 1][i] & 8) == 0);
                }

                if (i < maze[0].length - 1)
                {
                    tile.setSouth((maze[j][i + 1] & 1) == 0);
                }

                if( j == 7){
                    tile.setEast(true);
                }
                if( i == 7){
                    tile.setSouth(true);
                }

                tiles[j][i] = tile;
            }
        }
    }

    // Randomly deletes walls on the inside of the maze
    private void degradeWalls(int[][] maze) {
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if (j > 0 && Math.random() < DEGRADE_FACTOR) {
                    maze[i][j] |= 1;
                }

                if (i > 0 && Math.random() < DEGRADE_FACTOR) {
                    maze[i][j] |= 8;
                }
            }
        }
    }

    public Board(int x, int y){
        this.width = x;
        this.height = y;
        tiles = new Tile[x][y];
    }
    
    public Tile getTile(int x, int y){
        return tiles[x][y];
    }

    public Tile getTile(int tileNumber){
        return tiles[tileNumber % width][tileNumber / width];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
