package me.indian.logblock.history;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.indian.bds.event.Event;
import me.indian.util.logger.Logger;
import me.indian.bds.player.position.Position;
import me.indian.util.DateUtil;
import me.indian.logblock.LogBlockExtension;
import me.indian.logblock.util.MarkDownUtil;

public abstract class History {

    protected final Logger logger;
    private final LogBlockExtension logBlockExtension;
    private final String historyName, startDate;
    protected String startTime;

    public History(final LogBlockExtension logBlockExtension, final String historyName) {
        this.logBlockExtension = logBlockExtension;
        this.logger = this.logBlockExtension.getLogger();
        this.historyName = historyName;
        this.startDate = String.valueOf(LocalDate.now(DateUtil.POLISH_ZONE));
        this.startTime = DateUtil.getTimeHM().replace(":", "-");
    }

    public abstract void addToHistory(final Event eventToHistory);

    protected <T> boolean saveToFile(final Map<Position, Map<LocalDateTime, T>> map) {
        if (map.isEmpty()) return true;
        Event event = null;

        try {
            String date = String.valueOf(LocalDate.now(DateUtil.POLISH_ZONE));
            final String dateNow = String.valueOf(LocalDate.now(DateUtil.POLISH_ZONE));

            if (!dateNow.equals(this.startDate)) {
                date = this.startDate + "-" + dateNow;
            }

            final String saveTime = this.startTime + " - " + DateUtil.getTimeHM().replace(":", "-");

            final String path = this.logBlockExtension.getDataFolder() + File.separator + date + File.separator + this.historyName + File.separator;
            final File file = new File(path + saveTime + ".md");
            Files.createDirectories(Path.of(path));
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return false;
                }
            }


            try (final FileWriter writer = new FileWriter(file)) {
                writer.write("# Dostępne jest aż " + map.size() + " pozycji \n\n");

                for (final Map.Entry<Position, Map<LocalDateTime, T>> entry : map.entrySet()) {
                    final Map<LocalDateTime, T> l = entry.getValue();

                    final List<Map.Entry<LocalDateTime, T>> sortedEntries = new ArrayList<>(l.entrySet());
                    sortedEntries.sort(Map.Entry.comparingByKey());

                    for (final Map.Entry<LocalDateTime, T> sortedEntry : sortedEntries) {
                        final LocalDateTime dateTime = sortedEntry.getKey();
                        event = (Event) sortedEntry.getValue();

                        writer.write(MarkDownUtil.formatInfo(this.getTime(dateTime), event));
                    }
                }
            }

            this.logBlockExtension.getLogger().info("&aZapisano pomyślnie plik&e " + event.getEventName());
            return true;
        } catch (final Exception exception) {
            this.logBlockExtension.getLogger().critical("&cNie udało się zapisać pliku&e " + event.getEventName(), exception);
            return false;
        }
    }

    public abstract void saveHistory();

    protected abstract void clearHistory();

    protected String getTime(final LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"));
    }
}
