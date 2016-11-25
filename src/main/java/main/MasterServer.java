package main;

import accountserver.AccountServer;
import matchmaker.IMatchMaker;
import matchmaker.MatchMakerMultiplayer;
import matchmaker.MatchMakerSingleplayer;
import mechanics.Mechanics;
import messageSystem.MessageSystem;
import network.ClientConnectionServer;
import network.ClientConnections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import replication.FullStateReplicator;
import replication.Replicator;
import utils.IDGenerator;
import utils.SequentialIDGenerator;

import java.util.concurrent.ExecutionException;

/**
 * Created by apomosov on 14.05.16.
 */
public class MasterServer {
  @NotNull
  private final static Logger log = LogManager.getLogger(MasterServer.class);

  private void start() throws ExecutionException, InterruptedException {
    log.info("MasterServer started");
    //TODO RK3 configure server parameters
    ApplicationContext.instance().put(IMatchMaker.class, new MatchMakerSingleplayer());
    ApplicationContext.instance().put(ClientConnections.class, new ClientConnections());
    ApplicationContext.instance().put(Replicator.class, new FullStateReplicator());
    ApplicationContext.instance().put(IDGenerator.class, new SequentialIDGenerator());

    //TODO Add custom stuff here
    ApplicationContext.instance().put(IMatchMaker.class, new MatchMakerMultiplayer());



    MessageSystem messageSystem = new MessageSystem();
    ApplicationContext.instance().put(MessageSystem.class, messageSystem);

    Mechanics mechanics = new Mechanics();

    messageSystem.registerService(Mechanics.class, mechanics);
    messageSystem.registerService(AccountServer.class, new AccountServer(8080));
    messageSystem.registerService(ClientConnectionServer.class, new ClientConnectionServer(7000));
    messageSystem.getServices().forEach(Service::start);


    for (Service service : messageSystem.getServices()) {
      service.join();
    }
  }

  public static void main(@NotNull String[] args) throws ExecutionException, InterruptedException {
    MasterServer server = new MasterServer();
    server.start();
  }
}
