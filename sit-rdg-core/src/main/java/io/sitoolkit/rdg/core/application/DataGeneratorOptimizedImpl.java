package io.sitoolkit.rdg.core.application;

import io.sitoolkit.rdg.core.domain.generator.DataGeneratorFactory;
import io.sitoolkit.rdg.core.domain.generator.TableDataGenerator;
import io.sitoolkit.rdg.core.domain.generator.config.GeneratorConfig;
import io.sitoolkit.rdg.core.domain.generator.config.GeneratorConfigReader;
import io.sitoolkit.rdg.core.domain.schema.SchemaInfo;
import io.sitoolkit.rdg.core.infrastructure.DataWriter;
import io.sitoolkit.rdg.core.infrastructure.RowCounter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DataGeneratorOptimizedImpl implements DataGenerator {

  GeneratorConfigReader reader = new GeneratorConfigReader();

  NumberFormat f = NumberFormat.getNumberInstance();

  public List<Path> generate(Path input, List<Path> outDirs) {
    SchemaInfo schemaInfo = SchemaInfo.read(input);

    GeneratorConfig config = reader.read(input);

    List<TableDataGenerator> generators =
        DataGeneratorFactory.build(schemaInfo.getAllTables(), config);

    log.info(
        "Generating order : {}",
        generators.stream().map(TableDataGenerator::getTableName).collect(Collectors.joining(",")));

    List<Path> outFiles = generate(generators, outDirs, config.getLineSeparator());

    writeOrderFile(outFiles, outDirs, config.getLineSeparator());

    return outFiles;
  }

  List<Path> generate(List<TableDataGenerator> generators, List<Path> outDirs, String lineSeparator) {

    List<Path> outFiles = new ArrayList<>();
    int tableCount = generators.size();
    int generatedTableCount = 1;
    RowCounter rowCounter = new RowCounter();

    for (TableDataGenerator generator : generators) {

      long rowCount = generator.getRequiredRowCount();
      rowCounter.init(rowCount);

      log.info(
          "Start generating {} rows to {} {}/{}",
          f.format(rowCount),
          generator.getTableName(),
          generatedTableCount,
          tableCount);

      try (DataWriter writer = DataWriter.build(outDirs, generator.getTableName() + ".csv", lineSeparator)) {

        writer.writeAppend(generator.getHeader());

        for (long i = 0; i < rowCount; i++) {
          writer.writeAppend(generator.generateLine());

          if (rowCounter.isCheckPoint(i)) {
            log.info("{}/{} ({})%", f.format(i), f.format(rowCount), rowCounter.getProgressRate(i));
            rowCounter.next();
          }
        }

        generator.end();

        outFiles.addAll(writer.getFiles());

      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }

      generatedTableCount++;
    }

    return outFiles;
  }

  void writeOrderFile(List<Path> outFiles, List<Path> outDirs, String lineSeparator) {
    String orderString =
            outFiles.stream()
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(fileName -> fileName.replace(".csv", ""))
                    .collect(Collectors.joining(lineSeparator));

    for (Path outDir : outDirs) {
      Path orderFile = outDir.resolve("order.txt");
      try {
        Files.writeString(orderFile, orderString);
        log.info("Write: {}", orderFile);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }
}
