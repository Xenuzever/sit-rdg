package io.sitoolkit.rdg.core.domain.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class RelationDef {

  @EqualsAndHashCode.Include @Getter private Set<ColumnPair> columnPairs = new HashSet<>();

  @JsonIgnore
  public List<ColumnDef> getDistinctColumns() {
    return columnPairs.stream()
        .map(ColumnPair::getColumns)
        .flatMap(Collection::stream)
        .distinct()
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public List<TableDef> getTables() {
    return columnPairs.stream()
        .map(ColumnPair::getColumns)
        .flatMap(Collection::stream)
        .map(ColumnDef::getTable)
        .distinct()
        .collect(Collectors.toList());
  }

  public RelationDef(ColumnPair pair) {
    columnPairs.add(pair);
  }

  public void addAllPairs(Collection<ColumnPair> pairs) {
    columnPairs.addAll(pairs);
  }

  public boolean containsAnyInPair(ColumnPair pair) {
    return getDistinctColumns().parallelStream().anyMatch(col -> pair.getColumns().contains(col));
  }

  // public boolean belongsTo(TableDef table) {
  //   for (ColumnPair pair : getColumnPairs()) {
  //     TableDef mainTable = pair.getLeft().getTable();
  //     if (mainTable.equals(table)) {
  //       return true;
  //     }
  //     for (RelationDef relation : mainTable.getRelations()) {
  //       if (relation.belongsTo(table)) {
  //         return true;
  //       }
  //     }
  //   }
  //   return false;
  // }

  @JsonIgnore
  public TableDef getLeftTable() {
    return columnPairs.iterator().next().getLeft().getTable();
  }

  @JsonIgnore
  public TableDef getRightTable() {
    return columnPairs.iterator().next().getRight().getTable();
  }

  // TOTO lazy getter
  @JsonIgnore
  public Optional<RelationDef> getParent() {
    return getLeftTable().getRelations().stream().filter(this::isChildOf).findFirst();
  }

  @JsonIgnore
  public boolean isChildOf(RelationDef parent) {
    if (parent.columnPairs.size() != columnPairs.size() - 1) {
      return false;
    }

    for (ColumnPair parentPair : parent.columnPairs) {
      for (ColumnPair pair : columnPairs) {
        if (parentPair.getRight().equals(pair.getLeft())) {
          return true;
        }
      }
    }

    return false;
  }

  @JsonIgnore
  public boolean isParentOf(RelationDef child) {
    if (child.columnPairs.size() != columnPairs.size() + 1) {
      return false;
    }

    for (ColumnPair childPair : child.columnPairs) {
      for (ColumnPair pair : columnPairs) {
        if (childPair.getLeft().equals(pair.getRight())) {
          return true;
        }
      }
    }

    return false;
  }
}
