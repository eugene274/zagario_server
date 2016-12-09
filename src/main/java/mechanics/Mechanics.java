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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;
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
  private final List<Integer> playerSplit = new ArrayList<>();
  @NotNull
  private final List<Cell> notNullSpeedCells = new ArrayList<>();

  public Mechanics() {
    super("mechanics");
  }

  @Override
  public void run() {
    log.info(getAddress() + " started");
    Ticker ticker = new Ticker(this, 30);
    ticker.loop();
  }

  @Override
  public void tick(long elapsedNanos) {
    //TODO mechanics
    for (GameSession gs : ApplicationContext.instance().get(IMatchMaker.class).getActiveGameSessions()){

      gs.getFoodGenerator().tick(elapsedNanos);
      log.debug("FOOD " + gs.getField().getFoods().size());
      gs.getVirusGenerator().generate();
      log.debug("VIRUSES " + gs.getField().getViruses());

      List<Cell> toEat = new ArrayList<>();

      for (Player player : gs.getPlayers()){
        // moves
        if(playerMoves.containsKey(player.getId())){
          float vX = playerMoves.get(player.getId())[0];
          float vY = playerMoves.get(player.getId())[1];
          log.debug(String.format("MOVING PLAYER '%s' TO (%f,%f)",player.getName(),vX,vY));

          float avgX = 0;
          float avgY = 0;

          float dT = elapsedNanos/1000_000_000;
          float dX = (vX/10)*(dT/TIME_FACTOR)*gs.getField().getHeight();
          float dY = (vY/10)*(dT/TIME_FACTOR)*gs.getField().getHeight();

          log.debug("ELAPSED " + elapsedNanos);
          log.debug(String.format("DT = %f; DX = %f; DY = %f", dT, dX, dY));

          for (Cell c : player.getCells()){
            avgX += (float) c.getX()/player.getCells().size();
            avgY += (float) c.getY()/player.getCells().size();

            // eating food
            for(Food food : gs.getField().getFoods()){
              if(food.distance(c) <= Math.abs(c.getRadius() - food.getRadius())){
                c.setMass(c.getMass() + food.getMass());
                log.debug("PLAYER " + player + " eat food");
                toEat.add(food);
              }
            }


          }

          for(Cell victim : toEat){
            gs.getField().getFoods().remove(victim);
          }

          avgX += dX; avgY += dY;

          for (Cell c : player.getCells()){
              int inertness = (int) Math.ceil(Math.max(c.getMass(), MINIMAL_MASS)*INERTNESS_FACTOR);
            c.setX(c.getX() + (int)(
                    (avgX - c.getX())/inertness
            ));
            c.setY(c.getY() + (int)(
                    (avgY - c.getY())/inertness
            ));
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

                notNullSpeedCells.add(ejectedCell);

                gs.getField().setFreeCells(ejectedCell);
            }
          }
        }

          // split
          if (playerSplit.contains(player.getId())) {
            for (Cell cell : new ArrayList<>(player.getCells())) {
                int initMass = cell.getMass();
                if (initMass >= 2*MINIMAL_MASS) {
                    int halfMass = round(initMass/2);
                    cell.setMass(halfMass);
                    PlayerCell newCell = new PlayerCell(PlayerCell.idGenerator.next(),
                            cell.getX() + 50, cell.getY() + 50);
                    newCell.setMass(halfMass);
                    player.addCell(newCell);
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

    playerSplit.clear();
    playerEject.clear();
    playerMoves.clear();
    //execute all messages from queue
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
    int id = player.getId();
    if (!playerSplit.contains(id)) {
        playerSplit.add(id);
    }
    log.debug(player + " is about to split");
  }
}
