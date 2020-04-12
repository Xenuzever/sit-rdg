package io.sitoolkit.rdg.core.domain.generator;

import io.sitoolkit.rdg.core.domain.RowData;
import io.sitoolkit.rdg.core.domain.generator.config.GeneratorConfig;
import io.sitoolkit.rdg.core.domain.generator.config.ValueGenerator;
import io.sitoolkit.rdg.core.domain.schema.ColumnDef;
import io.sitoolkit.rdg.core.domain.schema.ColumnPair;
import io.sitoolkit.rdg.core.domain.schema.RelationDef;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RowDataGenerator {

  public static RowData generate(TableDef table, GeneratorConfig config) {
    RowData rowData = new RowData();

    for (ColumnDef column : table.getColumns()) {
      ValueGenerator generator = config.findValueGenerator(column);
      String generatedValue = generator.generate(column);
      rowData.put(column, generatedValue);
    }

    return rowData;
  }

  public static RowData generateUnrelated(
      TableDef table, RelationDef relation, GeneratorConfig config) {
    RowData rowData = new RowData();

    for (ColumnDef column : table.getColumns()) {
      if (!relation.getDistinctColumns().contains(column)) {
        continue;
      }
      ValueGenerator generator = config.findValueGenerator(column);
      String generatedValue = generator.generate(column);
      rowData.put(column, generatedValue);
    }

    return rowData;
  }

  public static RowData generate(RelationDef relation, GeneratorConfig config) {
    RowData rowData = new RowData();

    for (ColumnPair pair : relation.getColumnPairs()) {
      // TODO for primary key column
      ColumnDef column = pair.getLeft();
      ValueGenerator generator = config.findValueGenerator(column);
      rowData.put(column, generator.generate(column));
    }

    return rowData;
  }

  public static RowData filter(RowData rowData, RelationDef relation) {
    RowData filterd = new RowData();
    for (ColumnPair pair : relation.getColumnPairs()) {
      filterd.put(pair.getLeft(), rowData.get(pair.getLeft()));
    }
    return filterd;
  }

  public static void fill(RowData rowData, TableDef table, GeneratorConfig config) {
    for (ColumnDef column : table.getColumns()) {
      if (rowData.contains(column)) {
        continue;
      }

      ValueGenerator generator = config.findValueGenerator(column);
      rowData.put(column, generator.generate(column));
    }
  }

  public static RowData takeOverGenerate(
      RowData rowData, RelationDef parentRelation, RelationDef relation, GeneratorConfig config) {
    RowData newRowData = new RowData();

    for (ColumnPair parentPair : parentRelation.getColumnPairs()) {
      newRowData.put(parentPair.getRight(), rowData.get(parentPair.getLeft()));
    }

    for (ColumnPair pair : relation.getColumnPairs()) {
      ColumnDef left = pair.getLeft();
      if (newRowData.contains(left)) {
        continue;
      }
      ValueGenerator generator = config.findValueGenerator(left);
      newRowData.put(left, generator.generate(left));
    }

    return newRowData;
  }
}
