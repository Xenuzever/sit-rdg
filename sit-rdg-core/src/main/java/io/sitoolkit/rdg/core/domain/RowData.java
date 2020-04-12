package io.sitoolkit.rdg.core.domain;

import io.sitoolkit.rdg.core.domain.schema.ColumnDef;
import io.sitoolkit.rdg.core.domain.schema.ColumnPair;
import io.sitoolkit.rdg.core.domain.schema.RelationDef;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;

@ToString
public class RowData {

  @Getter private Map<String, String> valueMap = new HashMap<>();

  public void put(ColumnDef column, String value) {
    valueMap.put(column.getFullyQualifiedName(), value);
  }

  public boolean contains(ColumnDef column) {
    return valueMap.containsKey(column.getFullyQualifiedName());
  }

  public void putAll(RowData group) {
    this.valueMap.putAll(group.getValueMap());
  }

  public void putAllWithMainToSub(RowData rowData, RelationDef relation) {
    for (ColumnPair pair : relation.getColumnPairs()) {
      put(pair.getRight(), rowData.get(pair.getLeft()));
    }
  }

  public String get(ColumnDef column) {
    return valueMap.get(column.getFullyQualifiedName());
  }

  public void mergeRelatedData(RowData relatedRowData, RelationDef relation) {
    for (ColumnPair pair : relation.getColumnPairs()) {
      put(pair.getRight(), relatedRowData.get(pair.getLeft()));
    }
  }

  public List<Object> toList(List<ColumnDef> order) {
    return order.stream().map(this::get).collect(Collectors.toList());
  }
}
