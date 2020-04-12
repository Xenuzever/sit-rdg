package io.sitoolkit.rdg.core.domain.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemaDef {

  @JsonInclude(Include.NON_EMPTY)
  @JsonProperty("schemaName")
  private String name;

  @Builder.Default @JsonManagedReference private SortedSet<TableDef> tables = new TreeSet<>();

  @Builder.Default private List<RelationDef> relations = new ArrayList<>();

  @JsonIgnore
  public List<ColumnDef> getColumns() {
    return getTables().stream()
        .map(TableDef::getColumns)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  public Optional<ColumnDef> findColumnByQualifiedName(String qualifiedColumnName) {
    return getColumns().stream()
        .filter(
            column ->
                StringUtils.equalsIgnoreCase(column.getFullyQualifiedName(), qualifiedColumnName))
        .findFirst();
  }

  public void add(TableDef table) {
    tables.add(table);
    table.setSchema(this);
    table.setSchemaName(this.getName());
  }

  @JsonIgnore
  public Optional<TableDef> findTable(String tableName) {
    return tables.stream()
        .filter(table -> StringUtils.equalsIgnoreCase(table.getName(), tableName))
        .findFirst();
  }

  public List<RelationDef> findRelation(TableDef table) {

    return relations.stream()
        .filter(relation -> relation.getTables().contains(table))
        .collect(Collectors.toList());
  }

  public boolean equalsName(String name) {
    if (StringUtils.isEmpty(this.name)) {
      return StringUtils.isEmpty(name);
    }

    return StringUtils.equalsIgnoreCase(this.name, name);
  }

  public void afterDeserialize() {
    relations.stream().forEach(this::resolveRelationReferenceOfColumn);
  }

  void resolveRelationReferenceOfColumn(RelationDef relation) {

    for (ColumnPair pair : relation.getColumnPairs()) {
      ColumnDef left =
          findColumnByQualifiedName(pair.getLeft().getFullyQualifiedName()).orElseThrow();
      ColumnDef right =
          findColumnByQualifiedName(pair.getRight().getFullyQualifiedName()).orElseThrow();
      pair.reset(left, right);
      // columnInSchema.getRelations().add(relation);
    }
  }
}
