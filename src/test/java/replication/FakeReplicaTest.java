package replication;

import configuration.IniConfiguration;
import main.MasterServer;
import org.junit.Test;

/**
 * Created by eugene on 11/25/16.
 */
public class FakeReplicaTest {
  @Test
  public void fullState() throws Exception{
    IniConfiguration configuration = new IniConfiguration("src/test/resources/fake_replica_config.ini");
    MasterServer.main(new String[]{"test", "src/test/resources/fake_replica_config.ini"});
  }
}
