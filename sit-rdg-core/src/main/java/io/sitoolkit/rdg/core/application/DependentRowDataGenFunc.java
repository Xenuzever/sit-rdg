package io.sitoolkit.rdg.core.application;

import io.sitoolkit.rdg.core.domain.RowData;
import io.sitoolkit.rdg.core.domain.generator.RowDataGenerator;
import io.sitoolkit.rdg.core.domain.generator.config.GeneratorConfig;
import io.sitoolkit.rdg.core.domain.schema.RelationDef;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
class DependentRowDataGenFunc implements Function<TableDef, RowData> {

  DataStore dataStore;
  GeneratorConfig config;

  @Override
  public RowData apply(TableDef table) {
    RowData rowData = new RowData();

    for (RelationDef relation : table.getSortedRelations()) {
      if (table.equals(relation.getLeftTable())) {

        Optional<RelationDef> parentOpt = relation.getParent();

        if (parentOpt.isPresent()) {
          RowData storedData = dataStore.get(parentOpt.get());
          RowData newRelatedData =
              RowDataGenerator.takeOverGenerate(storedData, parentOpt.get(), relation, config);
          dataStore.put(relation, newRelatedData);
          rowData.putAll(newRelatedData);

          if (log.isTraceEnabled()) {
            log.trace("storedData: {}, newRelatedData: {}", storedData, newRelatedData);
          }

        } else {
          RowData relatedData = RowDataGenerator.generate(relation, config);
          dataStore.put(relation, relatedData);
          rowData.putAll(relatedData);

          if (log.isTraceEnabled()) {
            log.trace("relatedData: {}", relatedData);
          }
        }

      } else {
        RowData storedData = dataStore.get(relation);
        rowData.putAllWithMainToSub(storedData, relation);

        if (log.isTraceEnabled()) {
          log.trace("storedData: {}", storedData);
        }
      }
      log.trace("rowData: {}", rowData);
    }
    RowDataGenerator.fill(rowData, table, config);

    return rowData;
  }
}
