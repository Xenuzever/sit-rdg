package io.sitoolkit.rdg.core.infrastructure;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVPrinter;

public class MappedCsvWriter implements Closeable {

  private Map<String, CSVPrinter> writerMap = new HashMap<>();

  public void write(String key, List<Object> line) throws IOException {
    writerMap.get(key).printRecord(line);
  }

  public void close(String key) throws IOException {
    writerMap.get(key).close();
  }

  @Override
  public void close() throws IOException {
    // TODO
    for (CSVPrinter csvPrinter : writerMap.values()) {
      csvPrinter.close();
    }
  }
}
