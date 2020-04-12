package io.sitoolkit.rdg.core.infrastructure;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

@Slf4j
public class CsvWriter implements DataWriter {

  private CSVPrinter printer;

  private Path out;

  public CsvWriter(Path out, CSVFormat format) throws IOException {
    this.out = out;
    this.printer = new CSVPrinter(new FileWriter(out.toString()), format);
  }

  public CsvWriter(Path out) throws IOException {
    this(out, CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator()));
  }

  public void writeAppend(List<Object> line) throws IOException {
    printer.printRecord(line);
    printer.flush();
  }

  @Override
  public void close() throws IOException {
    printer.close();
  }

  @Override
  public List<Path> getFiles() {
    return List.of(out);
  }
}
