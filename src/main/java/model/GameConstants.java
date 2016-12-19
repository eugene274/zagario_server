package model;

/**
 * @author apomosov
 */
public interface GameConstants {
  int MAX_PLAYERS_IN_SESSION = 10;
  int FIELD_WIDTH = 1000;
  int FIELD_HEIGHT = 1000;
  int FOOD_MASS = 10;
  int DEFAULT_PLAYER_CELL_MASS = 40;
  // TODO: return - int VIRUS_MASS = 100;
  int VIRUS_MASS = 20; // for test
  int FOOD_PER_SECOND_GENERATION = 1;
  int MAX_FOOD_ON_FIELD = 10;
  // TODO: return - int NUMBER_OF_VIRUSES = 10;
  int NUMBER_OF_VIRUSES = 3; // for test
}
