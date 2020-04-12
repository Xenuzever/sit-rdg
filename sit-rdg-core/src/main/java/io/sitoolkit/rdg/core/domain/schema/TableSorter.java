package io.sitoolkit.rdg.core.domain.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableSorter {

  public static List<TableDef> sortByDependency(List<TableDef> tables) {

    List<TableDef> sortedTables = new ArrayList<>();

    for (TableDef table : tables) {

      int index = 0;
      int candidate = 0;

      for (TableDef sortedTable : sortedTables) {
        if (table.dependsOn(sortedTable)) {
          candidate = index + 1;
        } else if (sortedTable.dependsOn(table)) {
          candidate = index;
          break;
        }

        if (log.isTraceEnabled()) {
          log.trace("{} vs {}({}) = {}", table.getName(), sortedTable.getName(), index, candidate);
        }
        index++;
      }
      sortedTables.add(candidate, table);

      if (log.isTraceEnabled()) {
        log.trace(
            "sortedTables: {}",
            sortedTables.stream().map(TableDef::getName).collect(Collectors.joining(",")));
      }
    }

    return sortedTables;
  }
}
