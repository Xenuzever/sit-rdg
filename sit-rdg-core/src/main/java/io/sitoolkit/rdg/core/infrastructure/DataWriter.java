package io.sitoolkit.rdg.core.infrastructure;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DataWriter extends Closeable {

  void writeAppend(List<Object> line) throws IOException;

  List<Path> getFiles();

  static DataWriter build(List<Path> outDirs, String fileName) throws IOException {

    Logger log = LoggerFactory.getLogger(DataWriter.class);

    for (Path outDir : outDirs) {
      if (!outDir.toFile().exists()) {
        outDir.toFile().mkdirs();
        log.info("Make directory: {}", outDir);
      }
    }

    if (outDirs.size() == 1) {
      Path outFile = outDirs.get(0).resolve(fileName);
      log.info("Start Writing: {}", outFile);
      return new CsvWriter(outFile);
    } else {
      log.info("Start Writing: {} in {}", fileName, outDirs);
      return SmartCsvWriter.build(outDirs, fileName);
    }
  }
}
