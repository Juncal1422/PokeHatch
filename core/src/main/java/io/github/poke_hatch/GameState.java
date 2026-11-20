package io.github.poke_hatch;

import java.util.HashSet;

public class GameState {
  public int format = 1, trips, total, shinyBirths, slots = 1;
  public long coins, rc;
  public boolean keyUnlocked;
  public int[] counts = new int[650],
      since = new int[650],
      hatches = new int[16],
      level = new int[9],
      group = new int[9];
  public double[] progress = new double[9], charge = new double[9];
  public boolean[] dex = new boolean[650],
      shiny = new boolean[650],
      open = new boolean[16],
      active = new boolean[16],
      permanentGroup = new boolean[16],
      gen = {false, true, true, true, true, true};
  public HashSet<String> owned = new HashSet<>(), benefits = new HashSet<>();

  public GameState() {
    open[3] = active[3] = true;
    group[0] = 3;
  }
}
