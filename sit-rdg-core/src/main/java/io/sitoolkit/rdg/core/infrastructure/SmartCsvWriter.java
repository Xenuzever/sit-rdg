package io.sitoolkit.rdg.core.infrastructure;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmartCsvWriter implements DataWriter {

  private List<BufferedAsyncCsvWriter> writers = new ArrayList<>();

  private int writerIndex = 0;

  public static SmartCsvWriter build(List<Path> outDirPaths, String fileName) {
    SmartCsvWriter writer = new SmartCsvWriter();

    for (Path outDirPath : outDirPaths) {
      writer.writers.add(BufferedAsyncCsvWriter.build(outDirPath.resolve(fileName)));
    }

    return writer;
  }

  @Override
  public void writeAppend(List<Object> line) throws IOException {

    BufferedAsyncCsvWriter writer = writers.get(writerIndex++ % writers.size());

    if (writerIndex >= writers.size()) {
      writerIndex = 0;
    }

    writer.writeAppend(line);
  }

  public int getTotalRows() {
    return writers.stream().mapToInt(BufferedAsyncCsvWriter::getTotalRows).sum();
  }

  @Override
  public void close() throws IOException {

    writers.stream()
        .forEach(
            writer -> {
              try {
                writer.close();
              } catch (IOException e) {
                log.warn("Exception occurs when closing.", e);
              }
            });
  }

  @Override
  public List<Path> getFiles() {
    return writers.stream()
        .map(BufferedAsyncCsvWriter::getFiles)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }
}
