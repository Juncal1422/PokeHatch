package io.github.poke_hatch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import java.util.*;

public class Catalog {
  public static class Species {
    public int id, gen, parent, root, n;
    public String name, item;
    public int[] groups, pool;
    public boolean extra, ice, fossil;
    public String[] drops;
  }

  public static class Group {
    public int id, cost, steps;
    public String name, boost, benefit;
  }

  public static class Gate {
    public int id, shiny, total, badges, purchases, evol, baby, g;
    public String requires;
  }

  public static class Item {
    public String id, name, desc, kind, section, source;
    public int price, click, rate, choice, group;
    public int[] scent, scarf;
    public boolean evolution;
    public Gate gate;
  }

  private static <T> T read(String file, Class<T> type) {
    return new Json().fromJson(type, Gdx.files.classpath("pokehatch/" + file + ".json"));
  }

  final Species[] species = read("species", Species[].class);
  final Item[] items = read("items", Item[].class);
  final Group[] groups = read("groups", Group[].class);
  final String[] snacks = strings("snacks"),
      balls = strings("balls"),
      potions = strings("potions"),
      regions = strings("regions"),
      badges = strings("badges"),
      badgeNames = strings("badgeNames");
  final int[] slots = read("slots", int[].class);
  final Map<String, Item> item = new HashMap<>();
  final Species[] byId = new Species[650];
  final Group[] byGroup = new Group[16];

  private String[] strings(String file) {
    return read(file, String[].class);
  }

  final boolean[] hasChildren = new boolean[650];
  final boolean[][] eligibleByGen = new boolean[16][6];
  final Map<Integer, List<Species>> families = new HashMap<>();

  Catalog() {
    for (Species p : species) {
      if (p.parent > 0) hasChildren[p.parent] = true;
      families.computeIfAbsent(p.root, k -> new ArrayList<>()).add(p);
      for (int g : p.pool) eligibleByGen[g][p.gen] = true;
    }
    for (Item i : items) item.put(i.id, i);
    for (Species s : species) byId[s.id] = s;
    for (Group g : groups) byGroup[g.id] = g;
  }

  static boolean contains(int[] list, int n) {
    for (int x : list) if (x == n) return true;
    return false;
  }
}
