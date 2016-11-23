package messageSystem.messages;

import main.ApplicationContext;
import mechanics.Mechanics;
import messageSystem.Abonent;
import messageSystem.Message;
import messageSystem.MessageSystem;
import model.Player;

/**
 * Created by eugene on 11/23/16.
 */
public class EjectMassMsg extends Message {
    private Player player;

    public EjectMassMsg(@org.jetbrains.annotations.NotNull Player player) {
        super(null, ApplicationContext.instance().get(MessageSystem.class).getService(Mechanics.class).getAddress());
        Message.log.info("Command eject mass received for " + player);
    }

    @Override
    public void exec(Abonent abonent) {
        //TODO
    }
}
