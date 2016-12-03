package network;

import main.ApplicationContext;
import main.Service;
import messageSystem.Abonent;
import messageSystem.MessageSystem;
import model.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.jetbrains.annotations.NotNull;
import ticker.Tickable;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by eugene on 12/3/16.
 */
public class Kicker extends Service {
    private static int kickInterval = 300;
    public static final Logger log = LogManager.getLogger(Kicker.class);
    @NotNull
    private final ClientConnections connections;


    public Kicker() {
        super("kicker");
        connections = ApplicationContext.instance().get(ClientConnections.class);
    }

    @Override
    public void run() {
        while (true){
            connections.getConnections().stream()
                    .filter(playerSessionEntry -> !playerSessionEntry.getValue().isOpen())
                    .forEach(playerSessionEntry -> kick(playerSessionEntry.getKey()));

            try {
                Thread.sleep(kickInterval);
            } catch (InterruptedException e) {
                log.warn("INTERRUPTED");
            }

            ApplicationContext.instance().get(MessageSystem.class).execForService(this);
        }
    }

    public void kick(Player player){
        log.info("Player " + player + "kicked");
    }
}
