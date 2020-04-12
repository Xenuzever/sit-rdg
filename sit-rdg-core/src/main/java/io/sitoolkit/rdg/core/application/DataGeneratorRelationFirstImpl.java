package io.sitoolkit.rdg.core.application;

import io.sitoolkit.rdg.core.domain.RowData;
import io.sitoolkit.rdg.core.domain.generator.config.GeneratorConfig;
import io.sitoolkit.rdg.core.domain.generator.config.GeneratorConfigReader;
import io.sitoolkit.rdg.core.domain.schema.ColumnDef;
import io.sitoolkit.rdg.core.domain.schema.SchemaInfo;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import io.sitoolkit.rdg.core.domain.schema.TableSorter;
import io.sitoolkit.rdg.core.infrastructure.DataWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataGeneratorRelationFirstImpl implements DataGenerator {

  GeneratorConfigReader reader = new GeneratorConfigReader();

  DataStore dataStore = new DataStore();

  public List<Path> generate(Path input, List<Path> outDirs) {
    SchemaInfo schemaInfo = SchemaInfo.read(input);

    GeneratorConfig config = reader.read(input);

    IndependentRowDataGenFunc indepGen = new IndependentRowDataGenFunc(dataStore, config);
    DependentRowDataGenFunc depGen = new DependentRowDataGenFunc(dataStore, config);

    List<TableDef> independentTables = new ArrayList<>();
    List<TableDef> dependentTables = new ArrayList<>();

    for (TableDef table : schemaInfo.getAllTables()) {
      if (table.isDependent()) {
        dependentTables.add(table);
      } else {
        independentTables.add(table);
      }
    }

    List<Path> outFiles = generate(independentTables, config, outDirs, indepGen);

    List<TableDef> sortedDenendentTables = TableSorter.sortByDependency(dependentTables);

    outFiles.addAll(generate(sortedDenendentTables, config, outDirs, depGen));

    return outFiles;
  }

  List<Path> generate(
      List<TableDef> tables,
      GeneratorConfig config,
      List<Path> outDirs,
      Function<TableDef, RowData> rowDataGenFunc) {

    List<Path> outFiles = new ArrayList<>();

    for (TableDef table : tables) {

      int rowCount = config.getRowCount(table);

      try (DataWriter writer = DataWriter.build(outDirs, toFileName(table))) {

        writer.writeAppend(toHeader(table));

        for (int i = 0; i < rowCount; i++) {
          RowData rowData = rowDataGenFunc.apply(table);
          writer.writeAppend(rowData.toList(table.getColumns()));
        }

        outFiles.addAll(writer.getFiles());

      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    return outFiles;
  }

  String toFileName(TableDef table) {
    return table.getFullyQualifiedName() + ".csv";
  }

  List<Object> toHeader(TableDef table) {
    return table.getColumns().stream().map(ColumnDef::getName).collect(Collectors.toList());
  }
}
