package io.github.poke_hatch;

import static io.github.poke_hatch.Catalog.*;

import java.util.*;

public class GameEngine {
  private static final int BUG_GROUP_ID = 3;

  enum HatchReward {
    FIRST,
    REPEAT,
    SHINY,
    SHINY_REPEAT
  }

  final Catalog c;
  GameState s;
  final Random random = new Random();

  interface Listener {
    void hatch(Species species, boolean shiny, HatchReward reward, long coins, List<String> drops);

    void step(int slot);
  }

  Listener listener;
  int revision;
  final int[] levels = {25, 50, 100, 250, 500};
  final double[] rates = {0, .25, .5, 1, 1.5, 2};

  GameEngine(Catalog catalog, GameState state) {
    c = catalog;
    s = state;
  }

  boolean has(String id) {
    return s.owned.contains(id) || s.benefits.contains(id);
  }

  int flag(String id) {
    return has(id) ? 1 : 0;
  }

  int sum(String[] keys) {
    int n = 0;
    for (String k : keys) n += flag(k);
    return n;
  }

  int registered() {
    int n = 0;
    for (boolean v : s.dex) if (v) n++;
    return n;
  }

  int evolutions() {
    int n = 0;
    for (Species p : c.species) if (p.parent != 0 && s.dex[p.id]) n++;
    return n;
  }

  boolean completeGroup(int g) {
    for (Species p : c.species) if (contains(p.groups, g) && !s.dex[p.id]) return false;
    return true;
  }

  boolean completeRegion(int gen) {
    for (Species p : c.species) if (p.gen == gen && !s.dex[p.id]) return false;
    return true;
  }

  void milestones() {
    int ev = evolutions();
    for (int i = 0; i < 5; i++) {
      if (ev >= 64 * (i + 1)) s.owned.add(c.snacks[i]);
      if (completeRegion(i + 1)) {
        s.owned.add(c.balls[i]);
        s.keyUnlocked = true;
      }
    }
    for (Group g : c.groups) if (completeGroup(g.id)) s.permanentGroup[g.id] = true;
    if (registered() == c.species.length) s.owned.add("master-ball");
  }

  void shinyAwards() {
    boolean[] covered = new boolean[16];
    for (Species p : c.species) if (s.shiny[p.id]) for (int g : p.groups) covered[g] = true;
    boolean all = true;
    for (Group g : c.groups) all &= covered[g.id];
    if (all) s.owned.add("iris-ticket");
    for (Item i : c.items)
      if (i.scarf.length > 0) {
        int count = 0;
        for (Species p : c.species)
          if (s.shiny[p.id])
            for (int g : i.scarf)
              if (contains(p.groups, g)) {
                count++;
                break;
              }
        if (count >= 3) s.owned.add(i.id);
      }
  }

  void inherit(int root) {
    for (Species p : c.species) if (p.root == root && s.dex[p.id]) s.shiny[p.id] = true;
    shinyAwards();
  }

  void register(Species p) {
    s.dex[p.id] = true;
    s.since[p.id] = s.counts[p.root];
    milestones();
  }

  int purchases() {
    int n = s.slots - 1;
    for (String k : s.owned) if (c.item.get(k).kind.equals("PC")) n++;
    return n;
  }

  String gate(Item i) {
    return requirement(i, true);
  }

  String requirement(Item i, boolean unmetOnly) {
    Gate g = i.gate;
    if (i.kind.equals("RC") && s.trips == 0 && unmetOnly) return "Viaja con la GS Ball";
    if (i.group > 0 && (!unmetOnly || !s.permanentGroup[i.group]))
      return "Completa el grupo " + c.byGroup[i.group].name;
    if (i.id.equals("card-key") && (!unmetOnly || !s.keyUnlocked)) return "Completa una generación";
    if (g.id > 0 && (!unmetOnly || !s.dex[g.id])) return "Registra a " + c.byId[g.id].name;
    if (g.requires != null && (!unmetOnly || !has(g.requires)))
      return "Compra " + c.item.get(g.requires).name;
    if (g.badges > 0 && (!unmetOnly || evolutions() < 64 * g.badges))
      return "Completa las medallas de " + c.regions[g.badges - 1];
    if (g.baby > 0 && (!unmetOnly || s.counts[g.baby] < (has("rare-incense") ? 13 : 25)))
      return "Eclosiona " + (has("rare-incense") ? 13 : 25) + " " + c.byId[g.baby].name;
    if (g.purchases > 0 && (!unmetOnly || purchases() < g.purchases))
      return "Compra " + g.purchases + " objetos";
    if (g.g > 0 && (!unmetOnly || s.hatches[g.g] < 40))
      return "40 eclosiones de " + c.byGroup[g.g].name;
    if (g.shiny > 0 && (!unmetOnly || s.shinyBirths < g.shiny))
      return g.shiny + " eclosiones shiny";
    if (g.total > 0 && (!unmetOnly || s.total < g.total)) return g.total + " eclosiones";
    if (g.evol > 0 && (!unmetOnly || evolutions() < g.evol)) return g.evol + " evoluciones";
    return "";
  }

  int price(int base) {
    return has("discount-coupon") ? base * 9 / 10 : base;
  }

  int price(Item i) {
    if (i.kind.equals("RC") || i.id.equals("gs-ball")) return i.price;
    return price(i.price) / (i.evolution && has("odd-stone") ? 2 : 1);
  }

  boolean buy(Item i) {
    if (i.kind.equals("drop") || has(i.id) || !gate(i).isEmpty()) return false;
    int cost = price(i);
    if (i.kind.equals("RC")) {
      if (s.rc < cost) return false;
      s.rc -= cost;
      if (i.id.equals("parcel")) s.coins += 5;
      else s.benefits.add(i.id);
    } else {
      if (s.coins < cost) return false;
      s.coins -= cost;
      if (i.id.equals("gs-ball")) travel();
      else s.owned.add(i.id);
    }
    revision++;
    return true;
  }

  void travel() {
    GameState next = new GameState();
    next.trips = s.trips + 1;
    next.rc = s.rc + registered();
    next.benefits = s.benefits;
    next.permanentGroup = s.permanentGroup;
    next.keyUnlocked = s.keyUnlocked;
    next.gen = s.gen;
    if (has("journal")) {
      next.shiny = s.shiny;
      for (Species p : c.species) if (next.shiny[p.id]) next.dex[p.id] = true;
    }
    s = next;
    milestones();
    shinyAwards();
  }

  boolean spend(int base) {
    int cost = price(base);
    if (s.coins < cost) return false;
    s.coins -= cost;
    revision++;
    return true;
  }

  boolean unlock(Group g) {
    if (s.open[g.id] || !spend(g.cost)) return false;
    s.open[g.id] = s.active[g.id] = true;
    return true;
  }

  boolean expand() {
    if (s.slots >= 9 || !spend(c.slots[s.slots - 1])) return false;
    s.group[s.slots++] = nextGroup();
    return true;
  }

  boolean level(int slot) {
    int lv = s.level[slot];
    if (lv == 5 || !spend(levels[lv])) return false;
    s.level[slot]++;
    return true;
  }

  boolean eligible(int g) {
    for (int i = 1; i <= 5; i++) if (s.gen[i] && c.eligibleByGen[g][i]) return true;
    return false;
  }

  int nextGroup() {
    List<Integer> ok = new ArrayList<>(), active = new ArrayList<>();
    for (Group g : c.groups)
      if (s.active[g.id]) {
        active.add(g.id);
        if (eligible(g.id)) ok.add(g.id);
      }
    return pick(ok.isEmpty() ? active : ok);
  }

  <T> T pick(List<T> a) {
    return a.get(random.nextInt(a.size()));
  }

  boolean chance(double n) {
    return random.nextDouble() < n;
  }

  double target(int g) {
    double speed = 1;
    for (String k : s.owned) {
      Item i = c.item.get(k);
      if (contains(i.scent, g)) speed++;
      else if (k.equals("lax-incense")) speed += .25;
      else if (k.equals("air-balloon") && g == 4) speed++;
    }
    Group group = c.byGroup[g];
    return group.steps
        * (has(group.boost) ? .75 : 1)
        * (has("full-incense") && (g == 3 || g == 6 || g == 4) ? 3 : 1)
        / speed
        * (has("master-ball") ? .5 : 1)
        * (has("life-orb") ? .75 : 1)
        * (has(group.benefit) ? .5 : 1);
  }

  boolean growing(Species p) {
    return c.hasChildren[p.id];
  }

  boolean pending(Species p) {
    for (Species q : c.families.get(p.root)) if (!s.dex[q.id]) return true;
    return false;
  }

  Species roll(int g) {
    List<Species> pool = new ArrayList<>(), grow = new ArrayList<>(), missing = new ArrayList<>();
    for (Species p : c.species)
      if (s.gen[p.gen] && contains(p.pool, g)) {
        pool.add(p);
        if (growing(p)) grow.add(p);
        if (pending(p)) missing.add(p);
      }
    int reduction = 0;
    for (String k : s.owned) reduction += c.item.get(k).choice;
    double single = (1 - reduction / 100.0) * (has("pure-incense") ? .5 : 1);
    Species p = pick(pool);
    if (!grow.isEmpty() && !grow.contains(p) && chance(1 - single)) p = pick(grow);
    if (!missing.contains(p) && !missing.isEmpty() && sum(c.balls) > 0) {
      double extra = grow.isEmpty() ? 0 : (pool.size() - grow.size()) * (1 - single) / grow.size(),
          total = 0;
      for (Species q : missing) total += grow.contains(q) ? 1 + extra : grow.isEmpty() ? 1 : single;
      if (chance(.2 * sum(c.balls) * total / (pool.size() - total))) {
        double roll = random.nextDouble() * total;
        for (Species q : missing) {
          roll -= grow.contains(q) ? 1 + extra : grow.isEmpty() ? 1 : single;
          if (roll < 0) {
            p = q;
            break;
          }
        }
      }
    }
    if (has("badge-case") && !grow.isEmpty() && !grow.contains(p) && chance(.5)) p = pick(grow);
    if (!missing.isEmpty() && !missing.contains(p) && chance(.1 * sum(c.potions)))
      p = pick(missing);
    return p;
  }

  int threshold(int n) {
    return (int) Math.ceil((has("exp-share") ? Math.ceil(n * .75) : n) * 1.2);
  }

  int required(Species p) {
    double base = p.n * .6;
    if (Math.ceil(base) > 20) base = Math.ceil(base) * .75;
    int n = threshold((int) Math.ceil(base));
    int required = has("rare-candy")
        ? Math.max(1, Math.min(n - 1, threshold((int) Math.ceil(base * .9))))
        : n;
    return p.groups.length == 1 && p.groups[0] == BUG_GROUP_ID
        ? required
        : (int) Math.ceil(required * 1.25);
  }

  int progress(Species p) {
    return s.counts[p.root] - (p.extra ? s.since[p.parent] : 0);
  }

  boolean ready(Species p) {
    return p.parent != 0
        && p.id != 292
        && s.dex[p.parent]
        && !s.dex[p.id]
        && progress(p) >= required(p)
        && (p.item.isEmpty() || has(p.item));
  }

  boolean evolve(Species p) {
    if (!ready(p)) return false;
    register(p);
    if (p.id == 291) register(c.byId[292]);
    s.coins +=
        (long) (25 * (1 + .5 * (sum(c.snacks) + flag("devon-parts"))) * (1 + flag("everstone")));
    if (s.shiny[p.root]) inherit(p.root);
    revision++;
    return true;
  }

  void drop(List<String> out, String id, boolean eligible, double probability) {
    if (eligible && !has(id) && chance(probability * (1 + flag("luck-incense")))) {
      out.add(id);
      s.owned.add(id);
    }
  }

  void hatch(int slot) {
    int g = s.group[slot];
    Species p = roll(g);
    double odds =
        (has("shiny-charm") ? .0025 : .0005) * (1 + flag("lustrous-orb")) * (1 + flag("case"));
    for (String k : s.owned) if (contains(c.item.get(k).scarf, g)) odds += .001;
    boolean shiny = chance(odds), fresh = !s.dex[p.id], knownShiny = s.shiny[p.id];
    boolean shinyRepeatBonus = !fresh && knownShiny && has("iris-ticket");
    s.counts[p.id]++;
    s.hatches[g]++;
    s.total++;
    if (fresh) register(p);
    int base = fresh ? 10 : flag("nugget") * (1 + flag("pearl") + 2 * flag("star-piece"));
    long gain =
        (long)
                ((base + (shiny ? 10 : 0))
                    * (1 + .5 * flag("amulet-coin") + .5 * flag("stardust") + flag("rare-bone")))
            * (1 + flag("blue-card"))
            * (shinyRepeatBonus ? 2 : 1);
    if (shiny) {
      s.shinyBirths++;
      inherit(p.root);
    }
    List<String> out = new ArrayList<>(), available = new ArrayList<>();
    for (String k : p.drops) if (!has(k)) available.add(k);
    if (!available.isEmpty()) drop(out, pick(available), true, .05);
    drop(out, "amulet-coin", p.id == 52, .05);
    drop(out, "ice-stone", p.ice, .025);
    drop(out, "rare-bone", p.id == 104, .01);
    drop(out, "air-balloon", g == 4, .001);
    drop(out, "light-ball", p.id == 172, .001);
    if (p.fossil) {
      double r = random.nextDouble() / ((1 + flag("luck-incense")) * (1 + flag("talisman")));
      if (r < .0016) {
        String k = r < .0001 ? "relic-gold" : r < .0006 ? "relic-silver" : "relic-copper";
        s.owned.add(k);
        out.add(k);
        gain += r < .0001 ? 2000 : r < .0006 ? 1000 : 500;
      }
    }
    s.coins += gain;
    s.progress[slot] = 0;
    s.group[slot] = nextGroup();
    revision++;
    HatchReward reward =
        shinyRepeatBonus
            ? HatchReward.SHINY_REPEAT
            : shiny ? HatchReward.SHINY : fresh ? HatchReward.FIRST : HatchReward.REPEAT;
    listener.hatch(p, shiny, reward, gain, out);
  }

  void care(int slot, double amount) {
    if (!eligible(s.group[slot])) return;
    s.progress[slot] += amount / target(s.group[slot]);
    listener.step(slot);
    if (s.progress[slot] >= 1 - 1e-10) hatch(slot);
  }

  void tap(int slot) {
    int power = 1;
    for (String k : s.owned) power += c.item.get(k).click;
    care(slot, power * (1 + flag("toxic-orb")));
  }

  void tick(float dt) {
    int rate = 100;
    for (String k : s.owned) rate += c.item.get(k).rate;
    for (int j = 0; j < s.slots; j++)
      if (s.level[j] > 0 && eligible(s.group[j])) {
        s.charge[j] += dt * rates[s.level[j]] * rate / 100 * (1 + flag("magma-emblem"));
        while (s.charge[j] >= 1) {
          s.charge[j]--;
          care(j, (1 + flag("flame-orb")) * (1 + flag("magma-stone")));
        }
      }
  }
}
