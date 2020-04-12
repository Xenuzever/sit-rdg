package io.sitoolkit.rdg.core.application;

import io.sitoolkit.rdg.core.domain.RowData;
import io.sitoolkit.rdg.core.domain.schema.RelationDef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

public class DataStore {
  Map<RelationDef, List<RowData>> store = new HashMap<>();

  public void put(RelationDef relation, RowData rowData) {
    List<RowData> rowDatas = store.computeIfAbsent(relation, k -> new ArrayList<>());
    rowDatas.add(rowData);
  }

  public RowData get(RelationDef relation) {

    List<RowData> rowDatas = store.get(relation);

    if (rowDatas == null) {
      throw new NoSuchElementException("No RowData stored for " + relation.toString());
    }
    int index = ThreadLocalRandom.current().nextInt(rowDatas.size() - 1);

    return rowDatas.get(index);
  }
}
