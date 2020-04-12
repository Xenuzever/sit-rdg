package io.sitoolkit.rdg.core.application;

import io.sitoolkit.rdg.core.domain.RowData;
import io.sitoolkit.rdg.core.domain.generator.RowDataGenerator;
import io.sitoolkit.rdg.core.domain.generator.config.GeneratorConfig;
import io.sitoolkit.rdg.core.domain.schema.RelationDef;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import java.util.function.Function;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class IndependentRowDataGenFunc implements Function<TableDef, RowData> {

  DataStore dataStore;
  GeneratorConfig config;

  @Override
  public RowData apply(TableDef table) {
    RowData rowData = RowDataGenerator.generate(table, config);

    for (RelationDef relation : table.getRelations()) {
      RowData relatedRowData = RowDataGenerator.filter(rowData, relation);
      dataStore.put(relation, relatedRowData);
    }

    return rowData;
  }
}
