package mechanics;

import main.ApplicationContext;
import main.Service;
import matchmaker.IMatchMaker;
import messageSystem.Message;
import messageSystem.MessageSystem;
import messageSystem.messages.ReplicateMsg;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ticker.Tickable;
import ticker.Ticker;
import utils.Timer;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.*;
import static mechanics.MechanicConstants.*;

/**
 * Created by apomosov on 14.05.16.
 */
public class Mechanics extends Service implements Tickable {
  @NotNull
  private final static Logger log = LogManager.getLogger(Mechanics.class);
  @NotNull
  private final Map<Integer,Float[]> playerMoves = new HashMap<>();
  @NotNull
  private final List<Integer> playerEject = new ArrayList<>();
  @NotNull
  private final Map<Player, utils.Timer> playerSplitTimers = new HashMap<>();
  @NotNull
  private final Map<Player, PlayerCell> cellToAdd = new HashMap<>();
  @NotNull
  private final Map<Player, Cell> cellToRemove = new HashMap<>();
  @NotNull
  private final List<Virus> virusToRemove = new ArrayList<>();

  public Mechanics() {
    super("mechanics");
  }

  @Override
  public void run() {
    log.info(getAddress() + " started");
    Ticker ticker = new Ticker(this, 40);
    ticker.loop();
  }

  private static void decrementSpeed(Cell cell, float dt){
    cell.setSpeedX( (cell.getSpeedX() > MINIMAL_SPEED)? cell.getSpeedX()*(1 - dt*VISCOSITY_SCALING/getViscosityDecrement(cell)) : 0.0f );
    cell.setSpeedY( (cell.getSpeedY() > MINIMAL_SPEED)? cell.getSpeedY()*(1 - dt*VISCOSITY_SCALING/getViscosityDecrement(cell)) : 0.0f );
  }

  private static void computeCoordinates(Cell cell, float dt){
    cell.setX(cell.getX() + (int)(cell.getSpeedX()*dt));
    cell.setY(cell.getY() + (int)(cell.getSpeedY()*dt));
  }

  private static float getViscosityDecrement(Cell cell){
    return VISCOSITY_DECREMENT*10/cell.getRadius();
  }

  @Override
  public void tick(long elapsedNanos) {
    float dT = elapsedNanos/1000_000f;
    log.debug("DT =  " + dT + " millis");

    //TODO mechanics
    for (GameSession gs : ApplicationContext.instance().get(IMatchMaker.class).getActiveGameSessions()){

      // Remove viruses
      virusToRemove.forEach(virus -> gs.getField().getViruses().remove(virus));
      virusToRemove.clear();

      // Add cells
      cellToAdd.forEach((player, cell) -> player.addCell(cell));
      cellToAdd.clear();

      // Remove cells
      cellToRemove.forEach((player, cell) -> player.removeCell(cell));
      cellToRemove.clear();

      gs.getFoodGenerator().tick(elapsedNanos);
      log.debug("FOOD " + gs.getField().getFoods().size());


      for (Cell cell : gs.getField().getFreeCells()){
        decrementSpeed(cell, dT);
        computeCoordinates(cell, dT);
      }



      for (Player player : gs.getPlayers()){
        // moves
        if(playerMoves.containsKey(player.getId())){
          float vX = playerMoves.get(player.getId())[0]/1000; // [ dx/millis ]
          float vY = playerMoves.get(player.getId())[1]/1000; // [ dy/millis ]
          log.debug(String.format("MOVING PLAYER '%s' TO (%f,%f)",player.getName(),vX,vY));

          float avgX = 0;
          float avgY = 0;

//          float dX = (vX/10)*(dT/TIME_FACTOR)*gs.getField().getHeight();
//          float dY = (vY/10)*(dT/TIME_FACTOR)*gs.getField().getHeight();

//          log.debug(String.format("DX = %f; DY = %f", dX, dY));

          for (Cell c : player.getCells()){
            avgX += (float) c.getX()/player.getCells().size();
            avgY += (float) c.getY()/player.getCells().size();
          }

//          avgX += dX; avgY += dY;

          for (Cell cell : player.getCells()){
              if(
                      abs(cell.getSpeedX()) > MAXIMAL_SPEED*1.2 || abs(cell.getSpeedY()) > MAXIMAL_SPEED*1.2 ||
                              ( playerSplitTimers.containsKey(player) && !playerSplitTimers.get(player).isExpired() )
                      ){
                decrementSpeed(cell, dT);
              }
              else {
                // returning force
                float rfX = (float) (- RETURNING_FORCE*pow(cell.getX() - gs.getField().getWidth()/2f,3.0)/pow(gs.getField().getWidth(), 4.0));
                float rfY = (float) (- RETURNING_FORCE*pow(cell.getY() - gs.getField().getHeight()/2f,3.0)/pow(gs.getField().getHeight(), 4.0));

                float speedX = (vX + (avgX - cell.getX())/ATTRACTION_DECREMENT + rfX)*getViscosityDecrement(cell);
                float speedY = (vY + (avgY - cell.getY())/ATTRACTION_DECREMENT + rfY)*getViscosityDecrement(cell);

                cell.setSpeedX((abs(speedX) > MAXIMAL_SPEED)? Math.signum(speedX)*MAXIMAL_SPEED : speedX);
                cell.setSpeedY((abs(speedY) > MAXIMAL_SPEED)? Math.signum(speedY)*MAXIMAL_SPEED : speedY);
              }

              computeCoordinates(cell, dT);


            // eating food
            for(Cell food : new HashSet<>(gs.getField().getFoods())){
              if(food.distance(cell) <= Math.abs(cell.getRadius())){
                cell.setMass(cell.getMass() + food.getMass());
                log.debug("PLAYER " + player + " eat food");
                gs.getField().getFoods().remove(food);
              }
            }

            // eating freeCell
            for(Cell freeCell : new ArrayList<>(gs.getField().getFreeCells())) {
              if (freeCell.distance(cell) <= Math.abs(cell.getRadius())) {
                cell.setMass(cell.getMass() + freeCell.getMass());
                log.debug("PLAYER " + player + " eat free cell");
                gs.getField().getFreeCells().remove(freeCell);
              }
            }

            // interact with virus
            if (cell.getMass() >= 1.2 * GameConstants.VIRUS_MASS) {
              for (Virus virus : gs.getField().getViruses()) {
                if (cell.distance(virus) <= cell.getRadius() + virus.getRadius()) {
                  int halfMass = cell.getMass() / 2;

                  cell.setMass(halfMass);

                  float angle = (float) (2*Math.PI*Math.random());
                  float dVx = (float)(SPLIT_SPEED*cos(angle));
                  float dVy = (float)(SPLIT_SPEED*sin(angle));

                  PlayerCell newCell = new PlayerCell(player.getId(), cell.getX(), cell.getY());
                  newCell.setMass(halfMass);
                  newCell.setSpeedX(cell.getSpeedX() - dVx);
                  newCell.setSpeedY(cell.getSpeedY() - dVy);

                  cellToAdd.put(player, newCell);
                  virusToRemove.add(virus);
                }
              }
            }

            // Collapse cells
            for (Cell anotherCell : new ArrayList<>(player.getCells())) {
                if ((cell != anotherCell ) && !cellToRemove.containsValue(anotherCell)
                        && !cellToRemove.containsValue(cell)) {
                    if (cell.distance(anotherCell) <= 0.5 * (cell.getRadius() + anotherCell.getRadius())) {
                        cellToRemove.put(player, anotherCell);
                        cell.setMass(anotherCell.getMass() + cell.getMass());
                    }
                }
            }
          }
        }

        // eject
        if (playerEject.contains(player.getId())) {

          for (Cell cell : player.getCells()) {
            int initMass = cell.getMass();
            if (initMass >= MINIMAL_MASS + EJECTED_MASS) {

                cell.setMass(initMass - EJECTED_MASS);

                int x = cell.getX() + cell.getRadius() + 50;
                int y = cell.getY() + cell.getRadius() + 50;

                Cell ejectedCell = new PlayerCell(-1, x, y);
                ejectedCell.setSpeedX(cell.getSpeedX() + 5);
                ejectedCell.setSpeedY(cell.getSpeedY() + 5);
                ejectedCell.setMass(EJECTED_MASS);

                gs.getField().setFreeCells(ejectedCell);
            }
          }
        }
      }

//      for (Cell cell : notNullSpeedCells) {
//        cell.setSpeedX(round(cell.getSpeedX()/1.2f));
//        cell.setSpeedY(round(cell.getSpeedY()/1.2f));
//      }
    }

    @NotNull MessageSystem messageSystem = ApplicationContext.instance().get(MessageSystem.class);

    Message replicateMsg = new ReplicateMsg(this.getAddress());

    log.info("Start replication");
    messageSystem.sendMessage(replicateMsg);

//    playerSplit.clear();
    playerEject.clear();
    playerMoves.clear();



    // execute all messages from queue
    messageSystem.execForService(this);
  }

  public void move(Player player, float dx, float dy){
    Float[] move = playerMoves.get(player.getId());
    if(move == null){
      playerMoves.put(player.getId(), new Float[]{dx,dy});
    }
    else {
      move[0] = dx; move[1] = dy;
    }
    log.debug(player + " is about to move");
  }

  public void eject(Player player){
    int id = player.getId();
    if (!playerEject.contains(id)) {
      playerEject.add(id);
    }
    log.debug(player + " is about to eject");
  }

  public void split(Player player){
    log.debug(player + " is about to split");

    if (!playerSplitTimers.containsKey(player) || playerSplitTimers.get(player).isExpired()){
      for (Cell cell : new ArrayList<>(player.getCells())) {
        int initMass = cell.getMass();
        if (initMass >= 2*MINIMAL_MASS) {
          float angle = (float) (2*Math.PI*Math.random());
          float dVx = (float)(SPLIT_SPEED*cos(angle));
          float dVy = (float)(SPLIT_SPEED*sin(angle));
          int halfMass = round(initMass/2);
          cell.setMass(halfMass);
          cell.setSpeedX(cell.getSpeedX() + dVx);
          cell.setSpeedY(cell.getSpeedY() + dVy);

          PlayerCell newCell = new PlayerCell(player.getId(),cell.getX(), cell.getY());
          newCell.setMass(halfMass);
          player.addCell(newCell);
          newCell.setSpeedX(cell.getSpeedX() - dVx);
          newCell.setSpeedY(cell.getSpeedY() - dVy);
        }
      }

      playerSplitTimers.put(player, new Timer(EJECTION_TIME, TimeUnit.MILLISECONDS));
    }
  }
}
