package io.sitoolkit.rdg.core.domain.schema;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TableDef implements Comparable<TableDef> {

  @ToString.Exclude @JsonBackReference private SchemaDef schema;

  @JsonIgnore private String schemaName;

  @JsonIgnore private String alias;

  @Getter(lazy = true)
  @EqualsAndHashCode.Include
  @JsonIgnore
  private final String fullyQualifiedName =
      StringUtils.isEmpty(schemaName) ? getName() : schema.getName() + "." + getName();

  @JsonProperty("tableName")
  private String name;

  @Builder.Default @JsonManagedReference private List<ColumnDef> columns = new ArrayList<>();

  @JsonIgnore
  public boolean isDependent() {

    for (RelationDef relation : getRelations()) {
      if (relation.getRightTable().equals(this)) {
        return true;
      }
    }
    return false;
  }

  @JsonIgnore
  public boolean dependsOn(TableDef table) {
    return getAncestors().contains(table);
  }

  @JsonIgnore
  public List<TableDef> getAncestors() {
    // TODO lazy getter
    List<TableDef> ancestors = new ArrayList<>();

    List<TableDef> parents =
        getRelations().stream()
            .map(RelationDef::getLeftTable)
            .filter(leftTable -> !leftTable.equals(this))
            .collect(Collectors.toList());

    ancestors.addAll(parents);

    for (TableDef parent : parents) {
      ancestors.addAll(parent.getAncestors());
    }

    return ancestors;
  }

  public Optional<ColumnDef> findColumn(String columnName) {
    return columns.stream().filter(column -> column.getName().equals(columnName)).findFirst();
  }

  @JsonIgnore
  public List<RelationDef> getRelations() {
    // return columns.stream()
    //     .map(ColumnDef::getRelations)
    //     .flatMap(List::stream)
    //     .collect(Collectors.toSet());
    return schema.findRelation(this);
  }

  @JsonIgnore
  public List<RelationDef> getSortedRelations() {
    List<RelationDef> result = new ArrayList<>();

    for (RelationDef rel : getRelations()) {
      if (rel.getLeftTable().equals(this)) {
        result.add(rel);
      } else {
        result.add(0, rel);
      }
    }

    return result;
  }

  @JsonIgnore
  public int getPkCnt() {
    int cnt = (int) getColumns().stream().filter(ColumnDef::isPrimaryKey).count();
    return cnt;
  }

  @Override
  public int compareTo(TableDef o) {

    if (getPkCnt() == o.getPkCnt()) {
      return getFullyQualifiedName().compareTo(o.getFullyQualifiedName());
    }

    if (getPkCnt() == 0) {
      return 1;
    }

    if (o.getPkCnt() == 0) {
      return -1;
    }

    return getPkCnt() - o.getPkCnt();
  }
}
