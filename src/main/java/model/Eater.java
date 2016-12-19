package model;

/**
 * Created by eugene on 11/21/16.
 */
public interface Eater {
  int getMass();
  void setMass(int mass);

  default void eat(Eatable victim){
    setMass(getMass() + victim.getMass());
  }

}
