# Labyrinth

## Description
A game in which one to two players explore a labyrinth searching for a treasure object. This is guarded by a foe who will chase the players and attack them, reducing their movements available per turn. Walls will become visible as they are run into. The Foe is invisible until it attacks. 

## Instructions
Load the game from main or Game.bat/Game.sh. Select a secret room for each player, clicking next turn after each. After this, you will use that to alternate between players who can move as many tiles as displayed. Players can only move to adjacent tiles, and can steal the treasure from one another by landing on the other while they have the treasure. 

## Design Patterns
  + MVC
    * Game, GameGraphics, and GameController
  + Observer pattern
    * GameController (Observer) and Game (Subject)
  + State Pattern
    * Game
  + Builder
    * Board, Tile
