package mechanics;

import main.ApplicationContext;
import main.Service;
import matchmaker.IMatchMaker;
import messageSystem.Message;
import messageSystem.MessageSystem;
import messageSystem.messages.ReplicateMsg;
import model.Cell;
import model.GameSession;
import model.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ticker.Tickable;
import ticker.Ticker;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static mechanics.MechanicConstants.MINIMAL_MASS;
import static mechanics.MechanicConstants.TIME_FACTOR;

/**
 * Created by apomosov on 14.05.16.
 */
public class Mechanics extends Service implements Tickable {
  @NotNull
  private final static Logger log = LogManager.getLogger(Mechanics.class);
  @NotNull
  private final Map<Integer,Float[]> playerMoves = new HashMap<>();

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

      for (Player player : gs.getPlayers()){
        // moves
        if(playerMoves.containsKey(player.getId())){
          float vX = playerMoves.get(player.getId())[0];
          float vY = playerMoves.get(player.getId())[1];
          log.debug(String.format("MOVING PLAYER '%s' TO (%f,%f)",player.getName(),vX,vY));

          float avgX = 0;
          float avgY = 0;

          float dT = elapsedNanos/1000_000;
          float dX = (vX/10)*(dT/TIME_FACTOR)*gs.getField().getHeight();
          float dY = (vY/10)*(dT/TIME_FACTOR)*gs.getField().getHeight();

          log.debug("ELAPSED " + elapsedNanos);
          log.debug(String.format("DT = %f; DX = %f; DY = %f", dT, dX, dY));

          for (Cell c : player.getCells()){
            avgX += (float) c.getX()/player.getCells().size();
            avgY += (float) c.getY()/player.getCells().size();
          }

          avgX += dX; avgY += dY;

          for (Cell c : player.getCells()){
            c.setX(c.getX() + (int)(
                    (avgX - c.getX())/Math.max(c.getMass(), MINIMAL_MASS)
            ));
            c.setY(c.getY() + (int)(
                    (avgY - c.getY())/Math.max(c.getMass(), MINIMAL_MASS)
            ));
          }

        }
      }
    }

    @NotNull MessageSystem messageSystem = ApplicationContext.instance().get(MessageSystem.class);

    Message replicateMsg = new ReplicateMsg(this.getAddress());

    log.info("Start replication");
    messageSystem.sendMessage(replicateMsg);

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
    // TODO
    log.debug(player + " is about to eject");
  }

  public void split(Player player){
    // TODO
    log.debug(player + " is about to split");
  }
}
