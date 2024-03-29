package dhadvtkv.proposed_tsb;

import dhadvtkv.common.Channel;
import dhadvtkv.common.Configurations;
import dhadvtkv.common.Coordinate;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class ProtocolMapperInit implements Control {

  static Map<Long, Type> nodeType = new HashMap<>();

  private String prefix;

  public enum Type {
    CLIENT,
    PARTITION,
    VALIDATOR
  }

  public ProtocolMapperInit(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public boolean execute() {
    try {
      new Configurations(
          Configuration.getInt(prefix + "." + "noPartitions", 8),
          Configuration.getInt(prefix + "." + "batchSize", 1),
          Configuration.getPid(prefix + "." + "protocolMapper"),
          Configuration.getDouble(prefix + "." + "bandwidth", 1),
          Configuration.getLong(prefix + "." + "cpuDelay", 1),
          Configuration.getBoolean(prefix + "." + "addCPUDelay", true),
          Configuration.getLong(prefix + "." + "batchTimeout", 500),
          Configuration.getLong(prefix + "." + "delayPerDistance", 1),
          Configuration.getLong(prefix + "." + "headerSize", 0));
      new Channel();

      String latenciesInfoFilePath = Configuration.getString(prefix + "." + "latenciesFile");

      List<String> latenciesInfo = Files.readAllLines(Paths.get(latenciesInfoFilePath));
      Map<Integer, Coordinate> nodesPositions = new HashMap<>();

      Node node = Network.get(Configurations.ROOT_ID);
      nodeType.put(node.getID(), Type.VALIDATOR);
      String[] rootCoordinates = latenciesInfo.get(0).split(" ");
      nodesPositions.put(
          Configurations.ROOT_ID,
          new Coordinate(
              Integer.parseInt(rootCoordinates[0]), Integer.parseInt(rootCoordinates[1])));

      for (int i = 0; i < Configurations.ROOT_ID; i++) {
        node = Network.get(i);
        nodeType.put(node.getID(), Type.PARTITION);
        String[] nodeCoordinates = latenciesInfo.get(i + 1).split(" ");
        nodesPositions.put(
            i,
            new Coordinate(
                Integer.parseInt(nodeCoordinates[0]), Integer.parseInt(nodeCoordinates[1])));
      }

      List<Coordinate[]> squares = new ArrayList<>();
      List<Integer> percentages = new ArrayList<>();

      for (int i = 0; i < Integer.parseInt(latenciesInfo.get(Configurations.ROOT_ID + 1)); i++) {
        String[] squareInfo = latenciesInfo.get(Configurations.ROOT_ID + 2 + i).split(" ");
        Coordinate pos0 =
            new Coordinate(Integer.parseInt(squareInfo[0]), Integer.parseInt(squareInfo[1]));
        Coordinate pos1 =
            new Coordinate(Integer.parseInt(squareInfo[2]), Integer.parseInt(squareInfo[3]));
        Coordinate[] square = {pos0, pos1};
        squares.add(square);

        if (i == 0) {
          percentages.add(Integer.parseInt(squareInfo[4]));
        } else {
          percentages.add(percentages.get(i - 1) + Integer.parseInt(squareInfo[4]));
        }
      }

      int squareIndex = 0;

      for (int i = Configurations.ROOT_ID + 1; i < Network.size(); i++) {
        node = Network.get(i);
        nodeType.put(node.getID(), Type.CLIENT);

        Coordinate[] square = squares.get(squareIndex);
        nodesPositions.put(i, Coordinate.getPoint(square[0], square[1]));
        if (percentages.get(squareIndex) < (i * 100.0) / Network.size()) {
          squareIndex++;
        }
      }

      Channel.setPositions(nodesPositions);

      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return true;
    }
  }
}
