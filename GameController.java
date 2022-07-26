import com.wwu.graphics.*;

// GameController
// Handles input, and updates the view
public class GameController implements CustomBoardGraphicsInf, Observer {
    private Game game; // Model
    private CustomGameGraphics graphics; // View
    
    public GameController(Game game) {
        this.game = game;
        game.registerObserver(this);
    }
    
    public void setGraphics(CustomGameGraphics graphics) {
        this.graphics = graphics;
        updateView();
    }

    // Handle RESET/NEXT button presses
    @Override
    public void buttonPressed(GraphicsClickTypes type) {
        System.out.println("buttonPressed: " + type);

        if (type == GraphicsClickTypes.RESET) {
            game.reset();
            clearBoardView();
            updateView();
        } else if (type == GraphicsClickTypes.NEXT) {
            game.next();
            //lazyUpdateView();
            updateView();
        } else if (type == GraphicsClickTypes.START) {
            // This button doesn't exist in the view
            System.out.println("How did we get here?");
        }
    }

    // Handle tile clicks with tile number
    @Override
    public void tilePressed(int tileNumber) {
        System.out.println("tilePressed: " + tileNumber);
    }

    // Handle tile clicks with tile coordinates
    @Override
    public void tilePressed(int column, int row) {
        System.out.println("tilePressed: column:"
                + column + " - row:" + row);

        game.selectTile(column, row);
        
        //game.processTurn(player, \game.getBoard().getTile(row, column));
        //lazyUpdateView();
        updateView();
    }

    // Update the view when the debug switch is toggled
    @Override
    public void debugChanged() {
        updateView();
    }

    // Handle observer updates
    @Override
    public void update(Object arg) {
        if (arg instanceof GameEvent) {
            handleEvent((GameEvent) arg);
        } else {
            System.out.println("Unknown update arg type: " + arg);
        }
    }

    // Handle model-independent game events
    private void handleEvent(GameEvent event) {
        switch (event.getType()) {
            case SHOW_MESSAGE:
                graphics.addTextToInfoArea(event.getMessage());
                break;
            
            case PLAY_SOUND:
                if (graphics.isSoundEnabled()) {
                    graphics.playSound(GraphicsSoundTypes.valueOf(event.getMessage()));
                }
                break;
        }
    }

    // Resets every tile image
    private void clearBoardView() {
        for (int i = 0; i < game.getBoard().getWidth(); i++) {
            for (int j = 0; j < game.getBoard().getHeight(); j++) {
                graphics.changeTileImage(i, j, GraphicImageTypes.TILE);
            }
        }
    }

    // Update the view from the current model state
    private void updateView() {
        Board board = game.getBoard();
        Player player1 = game.getPlayer1();
        Player player2 = game.getPlayer2();
        Foe foe = game.getFoe();
        Treasure treasure = game.getTreasure();

        graphics.setFoeAwake(foe != null && foe.isAwake());

        graphics.setActivePlayer(game.getActivePlayer());

        graphics.setPlayerInfoVisible(1, player1 != null);
        graphics.setPlayerInfoVisible(2, player2 != null);

        if (player1 != null) {
            graphics.setPlayerHasTreasure(1, player1 != null && player1.getHasTreasure());
            graphics.updatePlayerInfo(1, player1.getCurMovesPerTurn(), player1.getTotMovesPerTurn(), player1.getHealth());
        }

        if (player2 != null) {
            graphics.setPlayerHasTreasure(2, player2 != null && player2.getHasTreasure());
            graphics.updatePlayerInfo(2, player2.getCurMovesPerTurn(), player2.getTotMovesPerTurn(), player2.getHealth());
        }

        for (int i = 0; i < board.getHeight(); i++)
        {
            for (int j = 0; j < board.getWidth(); j++)
            {
                Tile tile = board.getTile(j, i);

                if (tile.hasAgent()) {
                    Agent agent = tile.getAgent1();
                    if (agent == player1) {
                        if (player1.getHasTreasure()) {
                            graphics.changeTileImage(j, i, ExtraGraphicImageTypes.HERO1_TREASURE);
                        } else {
                            if (tile.getAgent2() != null) {
                                graphics.changeTileImage(j, i, ExtraGraphicImageTypes.HERO1_HERO2);
                            } else {
                                graphics.changeTileImage(j, i, GraphicImageTypes.HERO1);
                            }
                        }
                    } else if (agent == player2) {
                        if (player2.getHasTreasure()) {
                            graphics.changeTileImage(j, i, ExtraGraphicImageTypes.HERO2_TREASURE);
                        } else {
                            if (tile.getAgent2() != null) {
                                graphics.changeTileImage(j, i, ExtraGraphicImageTypes.HERO2_HERO1);
                            } else {
                                graphics.changeTileImage(j, i, GraphicImageTypes.HERO2);
                            }
                        }
                    } else if (agent == foe) {
                        if (graphics.isDebug() || foe.isVisible()) {
                            graphics.changeTileImage(j, i, GraphicImageTypes.ANTAGONIST);
                        } else if (treasure != null && treasure.getTile() == tile && (graphics.isDebug() || treasure.isVisible())) {
                            graphics.changeTileImage(j, i, GraphicImageTypes.GOAL);
                        } else {
                            graphics.changeTileImage(j, i, GraphicImageTypes.TILE);
                        }
                    } else {
                        System.out.println("Unknown agent type: " + agent + " at tile " + tile.getX() + "," + tile.getY());
                        graphics.addTextToInfoArea("!! UNKNOWN AGENT !!");
                    }
                }
                else if (treasure != null && treasure.getTile() == tile && !treasure.isPickedUp() && (graphics.isDebug() || treasure.isVisible())) {
                    graphics.changeTileImage(j, i, GraphicImageTypes.GOAL);
                } else if (player1 != null && player1.getSecretRoom() == tile) {
                    graphics.changeTileImage(j, i, GraphicImageTypes.BASE1);
                } else if (player2 != null && player2.getSecretRoom() == tile) {
                    graphics.changeTileImage(j, i, GraphicImageTypes.BASE2);
                } else {
                    graphics.changeTileImage(j, i, GraphicImageTypes.TILE);
                }

                graphics.wallGraphicSetVisible(j,i, GraphicsWallDirections.NORTH, graphics.isDebug() ? tile.north() : tile.northVisible());
                graphics.wallGraphicSetVisible(j,i, GraphicsWallDirections.SOUTH, graphics.isDebug() ? tile.south() : tile.southVisible());
                graphics.wallGraphicSetVisible(j,i, GraphicsWallDirections.EAST, graphics.isDebug() ? tile.east() : tile.eastVisible());
                graphics.wallGraphicSetVisible(j,i, GraphicsWallDirections.WEST, graphics.isDebug() ? tile.west() : tile.westVisible());
            }
        }
    }
}
