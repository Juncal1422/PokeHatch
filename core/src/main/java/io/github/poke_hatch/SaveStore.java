package io.github.poke_hatch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.CRC32;

/** One local desktop profile, stored outside the installation directory. */
final class SaveStore {
  private static final int MAGIC = 0x50485331;
  private final Preferences prefs = Gdx.app.getPreferences("PokeHatch");
  private final Catalog catalog;
  private final Path file;
  private byte[] last;
  boolean blocked;
  int volume;

  SaveStore(Catalog catalog) {
    this.catalog = catalog;
    String appData = System.getenv("LOCALAPPDATA");
    Path base =
        appData == null || appData.isEmpty()
            ? Paths.get(System.getProperty("user.home"), ".local", "share")
            : Paths.get(appData);
    if (System.getProperty("os.name", "").startsWith("Mac"))
      base = Paths.get(System.getProperty("user.home"), "Library", "Application Support");
    file = base.resolve("PokeHatch").resolve("profile.sav");
    volume = Math.max(0, Math.min(100, prefs.getInteger("volume", 35)));
  }

  void volume(int value) {
    volume = value;
    prefs.putInteger("volume", value).flush();
  }

  GameState load() {
    if (!Files.exists(file)) return new GameState();
    try {
      if (Files.size(file) > 100000) throw new IOException("Tamaño inválido");
      byte[] bytes = Files.readAllBytes(file);
      GameState s = new GameState();
      try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
        if (in.readInt() != MAGIC) throw new IOException("Cabecera inválida");
        s.trips = in.readInt();
        s.total = in.readInt();
        s.shinyBirths = in.readInt();
        s.slots = in.readInt();
        s.coins = in.readLong();
        s.rc = in.readLong();
        s.keyUnlocked = in.readBoolean();
        for (int j = 0; j < s.counts.length; j++) s.counts[j] = in.readInt();
        for (int j = 0; j < s.since.length; j++) s.since[j] = in.readInt();
        for (int j = 0; j < s.hatches.length; j++) s.hatches[j] = in.readInt();
        for (int j = 0; j < s.level.length; j++) s.level[j] = in.readInt();
        for (int j = 0; j < s.group.length; j++) s.group[j] = in.readInt();
        for (int j = 0; j < s.progress.length; j++) s.progress[j] = in.readDouble();
        for (int j = 0; j < s.charge.length; j++) s.charge[j] = in.readDouble();
        for (int j = 0; j < s.dex.length; j++) s.dex[j] = in.readBoolean();
        for (int j = 0; j < s.shiny.length; j++) s.shiny[j] = in.readBoolean();
        for (int j = 0; j < s.open.length; j++) s.open[j] = in.readBoolean();
        for (int j = 0; j < s.active.length; j++) s.active[j] = in.readBoolean();
        for (int j = 0; j < s.permanentGroup.length; j++) s.permanentGroup[j] = in.readBoolean();
        for (int j = 0; j < s.gen.length; j++) s.gen[j] = in.readBoolean();
        readSet(in, s.owned);
        readSet(in, s.benefits);
        CRC32 crc = new CRC32();
        crc.update(bytes, 0, bytes.length - 8);
        if (in.readLong() != crc.getValue() || in.read() != -1)
          throw new IOException("Partida incompleta");
      }
      check(
          s.format == 1
              && s.slots >= 1
              && s.slots <= 9
              && s.coins >= 0
              && s.rc >= 0
              && s.total >= 0
              && s.trips >= 0);
      check(
          s.dex.length == 650
              && s.shiny.length == 650
              && s.counts.length == 650
              && s.since.length == 650);
      check(
          s.open.length == 16
              && s.active.length == 16
              && s.hatches.length == 16
              && s.permanentGroup.length == 16);
      check(
          s.gen.length == 6
              && s.group.length == 9
              && s.level.length == 9
              && s.progress.length == 9
              && s.charge.length == 9);
      boolean active = false, gen = false;
      for (int i = 1; i <= 5; i++) gen |= s.gen[i];
      for (Catalog.Group g : catalog.groups) {
        if (s.active[g.id]) {
          check(s.open[g.id]);
          active = true;
        }
      }
      check(active && gen && s.open[3]);
      for (int i = 0; i < s.slots; i++) {
        check(
            s.group[i] > 0
                && s.group[i] < 16
                && catalog.byGroup[s.group[i]] != null
                && s.open[s.group[i]]);
        check(
            s.level[i] >= 0
                && s.level[i] <= 5
                && s.progress[i] >= 0
                && s.progress[i] < 1
                && s.charge[i] >= 0
                && s.charge[i] < 1);
      }
      for (String key : s.owned)
        check(catalog.item.containsKey(key) && !catalog.item.get(key).kind.equals("RC"));
      for (String key : s.benefits)
        check(
            catalog.item.containsKey(key)
                && catalog.item.get(key).kind.equals("RC")
                && !key.equals("parcel"));
      last = bytes;
      return s;
    } catch (IOException | RuntimeException e) {
      blocked = true;
      Gdx.app.error("PokeHatch", "Error al leer la partida", e);
      return new GameState();
    }
  }

  private void readSet(DataInputStream in, Set<String> keys) throws IOException {
    int n = in.readUnsignedShort();
    if (n > catalog.items.length) throw new IOException("Inventario inválido");
    for (int i = 0; i < n; i++) keys.add(in.readUTF());
  }

  private static void writeSet(DataOutputStream out, Set<String> keys) throws IOException {
    out.writeShort(keys.size());
    for (String k : new TreeSet<>(keys)) out.writeUTF(k);
  }

  void save(GameState s) {
    if (blocked) return;
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      try (DataOutputStream out = new DataOutputStream(bytes)) {
        out.writeInt(MAGIC);
        out.writeInt(s.trips);
        out.writeInt(s.total);
        out.writeInt(s.shinyBirths);
        out.writeInt(s.slots);
        out.writeLong(s.coins);
        out.writeLong(s.rc);
        out.writeBoolean(s.keyUnlocked);
        for (int value : s.counts) out.writeInt(value);
        for (int value : s.since) out.writeInt(value);
        for (int value : s.hatches) out.writeInt(value);
        for (int value : s.level) out.writeInt(value);
        for (int value : s.group) out.writeInt(value);
        for (double value : s.progress) out.writeDouble(value);
        for (double value : s.charge) out.writeDouble(value);
        for (boolean value : s.dex) out.writeBoolean(value);
        for (boolean value : s.shiny) out.writeBoolean(value);
        for (boolean value : s.open) out.writeBoolean(value);
        for (boolean value : s.active) out.writeBoolean(value);
        for (boolean value : s.permanentGroup) out.writeBoolean(value);
        for (boolean value : s.gen) out.writeBoolean(value);
        writeSet(out, s.owned);
        writeSet(out, s.benefits);
        CRC32 crc = new CRC32();
        crc.update(bytes.toByteArray());
        out.writeLong(crc.getValue());
      }
      byte[] next = bytes.toByteArray();
      if (Arrays.equals(last, next)) return;
      Files.createDirectories(file.getParent());
      Path tmp = Files.createTempFile(file.getParent(), "profile-", ".tmp");
      try {
        try (FileOutputStream stream = new FileOutputStream(tmp.toFile())) {
          stream.write(next);
          stream.getFD().sync();
        }
        try {
          Files.move(
              tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
          Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
      } finally {
        Files.deleteIfExists(tmp);
      }
      last = next;
    } catch (IOException e) {
      throw new IllegalStateException("Error al guardar la partida", e);
    }
  }

  private static void check(boolean valid) {
    if (!valid) throw new IllegalArgumentException("Partida inválida");
  }
}
