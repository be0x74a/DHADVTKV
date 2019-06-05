package DHADVTKV.common;

import peersim.core.CommonState;

public class Coordinate {

  private final int x;
  private final int y;

  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public static Coordinate getPoint(Coordinate coordinate0, Coordinate coordinate1) {

    int x =
        coordinate0.getX() < coordinate1.getX()
            ? coordinate0.getX() + CommonState.r.nextInt(coordinate1.getX() - coordinate0.getX())
            : coordinate1.getX() + CommonState.r.nextInt(coordinate0.getX() - coordinate1.getX());
    int y =
        coordinate0.getY() < coordinate1.getY()
            ? coordinate0.getY() + CommonState.r.nextInt(coordinate1.getY() - coordinate0.getY())
            : coordinate1.getY() + CommonState.r.nextInt(coordinate0.getY() - coordinate1.getY());
    return new Coordinate(x, y);
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Double distance(Coordinate pos1) {
    int x1 = pos1.getX();
    int y1 = pos1.getY();
    return Math.sqrt(Math.pow((x - x1), 2) + Math.pow(y - y1, 2));
  }
}
