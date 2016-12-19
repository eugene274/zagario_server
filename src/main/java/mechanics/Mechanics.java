package mechanics;

import main.ApplicationContext;
import main.Service;
import matchmaker.IMatchMaker;
import mechanics.forces.*;
import messageSystem.Message;
import messageSystem.MessageSystem;
import messageSystem.messages.ReplicateMsg;
import model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ticker.Tickable;
import ticker.Ticker;
import utils.EatComparator;
import utils.MathVector;
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
  private final Map<Player,Force> playerMoves = new HashMap<>();
  @NotNull
  private final List<Integer> playerEject = new ArrayList<>();
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

  private static void computeCoordinates(Cell cell, float dt){
    cell.setX((int) Math.ceil(cell.getX() + cell.getSpeed().cartesian(0)*dt));
    cell.setY((int) Math.ceil(cell.getY() + cell.getSpeed().cartesian(1)*dt));
  }

  private static void computeSpeed(Cell cell, MathVector force, float dt){
    cell.setSpeed(cell.getSpeed().plus(force.scale(dt)));
  }
  private Force viscosityForce = new ViscosityForce();

  @Override
  public void tick(long elapsedNanos) {
    float dT = TIME_SCALE*elapsedNanos/1000_000f;
    log.debug("DT =  " + dT + " millis");

    //TODO mechanics
    for (GameSession gs : ApplicationContext.instance().get(IMatchMaker.class).getActiveGameSessions()){
      TreeSet<Cell> viruses = new TreeSet<>(new EatComparator());
      viruses.addAll(gs.getField().getViruses());

      Force returningForce = new ReturningForce(gs.getField());

      // Remove viruses
      virusToRemove.forEach(virus -> gs.getField().getViruses().remove(virus));
      virusToRemove.clear();

      // Add cells
//      cellToAdd.forEach((player, cell) -> player.addCell(cell));
//      cellToAdd.clear();

      // Remove cells
//      cellToRemove.forEach((player, cell) -> player.removeCell(cell));
//      cellToRemove.clear();

      gs.getFoodGenerator().tick(elapsedNanos);
      log.debug("FOOD " + gs.getField().getFoods().size());


      for (Cell cell : gs.getField().getFreeCells()){
        MathVector force = returningForce.force(cell).minus(viscosityForce.force(cell));
        computeSpeed(cell, force, dT);
        computeCoordinates(cell, dT);
      }

      for (Player player : gs.getPlayers()){
        // moves

        Force attractionForce = new AttractionForce(player.getCells());
        Force repulsionForce = new RepulsionForce(player.getCells());

        Force mouseForce = playerMoves.getOrDefault(player,new NoForce());

        HashSet<PlayerCell> retainingCells = new HashSet<>(player.getCells());
        HashSet<PlayerCell> cellsToAdd = new HashSet<>();

        for (PlayerCell cell : player.getCells()){
          MathVector force = returningForce.force(cell).plus(
                  repulsionForce.force(cell).plus(
                          mouseForce.force(cell).plus(
                                  attractionForce.force(cell).minus(
                                          viscosityForce.force(cell)))));

          computeSpeed(cell, force, dT);
//            if(cell.getSpeed().magnitude() > MAXIMAL_SPEED){
//              cell.setSpeed(cell.getSpeed().direction().scale(MAXIMAL_SPEED));
//            }

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

          // interact with viruses
          for (Cell virus : viruses.headSet(cell)){

            if (cell.distance(virus) <= cell.getRadius() + virus.getRadius()) {
              int halfMass = cell.getMass() / 2;

                cell.setMass(halfMass);

                float angle = (float) (2*Math.PI*Math.random());
                float dVx = (float)(SPLIT_SPEED*cos(angle));
                float dVy = (float)(SPLIT_SPEED*sin(angle));

                PlayerCell newCell = new PlayerCell(player.getId(), cell.getX(), cell.getY());
                newCell.setMass(halfMass);
                newCell.setSpeed(cell.getSpeed().minus(new MathVector(new double[]{dVx,dVy})));

//                cellToAdd.put(player, newCell);
                virusToRemove.add((Virus) virus);

                cellsToAdd.add(newCell);
            }
          }

          // Collapse cells
          for (PlayerCell anotherCell : new HashSet<>(retainingCells)) {
            if (cell != anotherCell) {
              if (cell.distance(anotherCell) <= 0.5 * (cell.getRadius() + anotherCell.getRadius())) {
//                cellToRemove.put(player, anotherCell);
                cell.eat(anotherCell);
                retainingCells.remove(anotherCell);
              }
            }
          }

          // interaction with opponents
          TreeMap<PlayerCell, Player> opCells = new TreeMap<>(new EatComparator());
          HashSet<Player> opponents = new HashSet<>(gs.getPlayers());
          opponents.remove(player);

          for (Player opponent : opponents){
            for (PlayerCell opCell : opponent.getCells()){
              opCells.put(opCell, opponent);
            }
          }

          for( Map.Entry<PlayerCell, Player> opCell : opCells.headMap(cell).entrySet()){
            if(cell.distance(opCell.getKey()) <= cell.getRadius() + opCell.getKey().getRadius()) {
              cell.eat(opCell.getKey());
              opCell.getValue().getCells().remove(opCell.getKey());
            }
          }

        }

        player.getCells().retainAll(retainingCells);
        player.getCells().addAll(cellsToAdd);


        // eject
        if (playerEject.contains(player.getId())) {

          for (Cell cell : player.getCells()) {
            int initMass = cell.getMass();
            if (initMass >= MINIMAL_MASS + EJECTED_MASS) {

                cell.setMass(initMass - EJECTED_MASS);

                int x = (int) (cell.getX() + cell.getSpeed().direction().scale(cell.getRadius()).cartesian(0));
                int y = (int) (cell.getY() + cell.getSpeed().direction().scale(cell.getRadius()).cartesian(1));;

                Cell ejectedCell = new PlayerCell(-1, x, y);
                ejectedCell.setSpeed(cell.getSpeed().direction().scale(EJECT_SPEED));
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
    if(!playerMoves.containsKey(player)){
      playerMoves.put(player, new MouseForce(dx, dy));
    }
  }

  public void eject(Player player){
    int id = player.getId();
    if (!playerEject.contains(id)) {
      playerEject.add(id);
    }
    log.debug(player + " is about to eject");
  }

  public HashSet<PlayerCell> split(Player player){
    log.debug(player + " is about to split");
    HashSet<PlayerCell> emittedCells = new HashSet<>();

    for (Cell cell : new ArrayList<>(player.getCells())) {
      int initMass = cell.getMass();
      if (initMass >= 2*MINIMAL_MASS) {
        float angle = (float) (2*Math.PI*Math.random());
        MathVector direction = new MathVector(new double[] {cos(angle),sin(angle)});
        int halfMass = round(initMass/2);
        cell.setMass(halfMass);
        cell.setSpeed(cell.getSpeed().plus(direction.scale(SPLIT_SPEED)));
        cell.setRepulsionForce(new RepulsionForce(cell));

        PlayerCell newCell = new PlayerCell(player.getId(),cell.getX(), cell.getY());
        newCell.setMass(halfMass);
        player.addCell(newCell);
        newCell.setSpeed(cell.getSpeed().minus(direction.scale(SPLIT_SPEED)));
        newCell.setRepulsionForce(new RepulsionForce(newCell));

        emittedCells.add(newCell);
      }
    }
    return emittedCells;
  }
}
