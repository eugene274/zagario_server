package mechanics;

import main.ApplicationContext;
import main.Service;
import messageSystem.Message;
import messageSystem.MessageSystem;
import messageSystem.messages.ReplicateMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ticker.Tickable;
import ticker.Ticker;

/**
 * Created by apomosov on 14.05.16.
 */
public class Mechanics extends Service implements Tickable {
  @NotNull
  private final static Logger log = LogManager.getLogger(Mechanics.class);

  public Mechanics() {
    super("mechanics");
  }

  @Override
  public void run() {
    log.info(getAddress() + " started");
    Ticker ticker = new Ticker(this, 1);
    ticker.loop();
  }

  @Override
  public void tick(long elapsedNanos) {
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      log.error(e);
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }

    //TODO mechanics

    @NotNull MessageSystem messageSystem = ApplicationContext.instance().get(MessageSystem.class);

    Message replicateMsg = new ReplicateMsg(this.getAddress());

    log.info("Start replication");
    messageSystem.sendMessage(replicateMsg);

    //execute all messages from queue
    messageSystem.execForService(this);
  }
}
