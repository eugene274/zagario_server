package replication;

import org.jetbrains.annotations.TestOnly;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by eugene on 11/25/16.
 */
@TestOnly
public class FakeLeaderBoardReplicator implements Replicator {

  String readFile(String fileName) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(fileName));
    try {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append("\n");
        line = br.readLine();
      }
      return sb.toString();
    } finally {
      br.close();
    }
  }

  @Override
  public void replicate() {
    //TODO
    String leaders = "{}";
    try {
      leaders = readFile("fakeLeaders.json");
    } catch (IOException e) {
      e.printStackTrace();
    }


  }
}
