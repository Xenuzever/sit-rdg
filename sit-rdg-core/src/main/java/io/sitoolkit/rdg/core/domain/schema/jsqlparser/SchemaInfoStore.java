package io.sitoolkit.rdg.core.domain.schema.jsqlparser;

import io.sitoolkit.rdg.core.domain.schema.ColumnDef;
import io.sitoolkit.rdg.core.domain.schema.ColumnPair;
import io.sitoolkit.rdg.core.domain.schema.RelationDef;
import io.sitoolkit.rdg.core.domain.schema.SchemaDef;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Column;

@Slf4j
public class SchemaInfoStore {

  // key: Schema name, value: Tables
  // private Map<String, SortedSet<TableDef>> tablesInSchemaMap = new HashMap<>();

  // key: Schema name
  private Map<String, SchemaDef> schemaMap = new HashMap<>();

  // key: Column, value: relations
  private Map<ColumnDef, List<RelationDef>> relationsMap = new HashMap<>();

  // key: Alias, value: Table name
  private Map<String, String> aliasMap = new HashMap<>();

  public Set<SchemaDef> getSchemas() {

    // return tablesInSchemaMap.entrySet().stream()
    //     .map(
    //         e -> {
    //           String schemaName = e.getKey();
    //           SortedSet<TableDef> tables = e.getValue();
    //           return SchemaDef.builder().name(schemaName).tables(tables).build();
    //         })
    //     .collect(Collectors.toSet());

    return new HashSet<>(schemaMap.values());
  }

  public SchemaDef getSchema(String schemaName) {
    return schemaMap.get(schemaName);
  }

  public List<ColumnDef> getAllColumns() {
    // return tablesInSchemaMap.values().stream()
    //     .flatMap(Set::stream)
    //     .map(TableDef::getColumns)
    //     .flatMap(List::stream)
    //     .collect(Collectors.toList());

    return schemaMap.values().stream()
        .flatMap(schema -> schema.getColumns().stream())
        .collect(Collectors.toList());
  }

  void putAlias(String alias, String tableName) {
    aliasMap.put(alias, tableName);
  }

  String getActualTableName(String alias) {
    if (aliasMap.containsKey(alias)) {
      return aliasMap.get(alias);
    }
    return alias;
  }

  Optional<ColumnDef> getColumnDef(Column column) {

    String schemaName = column.getTable().getSchemaName();
    String tableName = getActualTableName(column.getTable().getName());
    String columnName = column.getColumnName();

    String fullyQualifiedName =
        Stream.of(schemaName, tableName, columnName)
            .filter(Objects::nonNull)
            .reduce((s1, s2) -> String.join(".", s1, s2))
            .orElse("");

    return getColumnDef(fullyQualifiedName);
  }

  Optional<ColumnDef> getColumnDef(String fullyQualifiedName) {

    // return tablesInSchemaMap.values().stream()
    //     .flatMap(Set::stream)
    //     .map(TableDef::getColumns)
    //     .flatMap(List::stream)
    //     .filter(column -> StringUtils.equals(column.getFullyQualifiedName(), fullyQualifiedName))
    //     .findFirst();

    return schemaMap.values().stream()
        .map(schema -> schema.findColumnByQualifiedName(fullyQualifiedName))
        .filter(colOpt -> !colOpt.isEmpty())
        .map(Optional::get)
        .findFirst();
  }

  void addTable(TableDef table) {
    String schema = table.getSchemaName();
    schemaMap
        .computeIfAbsent(
            schema,
            k ->
                SchemaDef.builder()
                    .name(schema)
                    .tables(new TreeSet<>())
                    .relations(new ArrayList<>())
                    .build())
        .getTables()
        .add(table);
    // tablesInSchemaMap.computeIfAbsent(schema, k -> new TreeSet<>()).add(table);
  }

  public Optional<TableDef> findTable(String schemaName, String tableName) {
    SchemaDef schema = schemaMap.get(schemaName);

    if (schema == null) {
      return Optional.empty();
    }

    return schema.findTable(tableName);
  }

  void addRelations(List<RelationDef> relations) {

    List<ColumnDef> columns =
        relations.stream()
            .map(RelationDef::getDistinctColumns)
            .flatMap(List::stream)
            .collect(Collectors.toList());

    List<ColumnDef> allCols =
        getAllColumns().stream().filter(columns::contains).collect(Collectors.toList());

    for (ColumnDef column : allCols) {

      List<RelationDef> gotRelations =
          relationsMap.computeIfAbsent(column, key -> new ArrayList<>(relations));

      for (RelationDef gotRelation : gotRelations) {
        List<ColumnDef> gotRelColumns = gotRelation.getDistinctColumns();
        for (RelationDef argRelation : relations) {
          argRelation.getDistinctColumns().stream()
              .filter(gotRelColumns::contains)
              .forEach(c -> gotRelation.addAllPairs(argRelation.getColumnPairs()));
        }
      }
    }
  }

  void mergeRelations() {

    List<ColumnDef> relationColumns =
        getAllColumns().stream().filter(relationsMap::containsKey).collect(Collectors.toList());

    for (ColumnDef column : relationColumns) {

      List<RelationDef> relationsInCol = relationsMap.get(column);
      column.setRelations(relationsInCol);

      // カラムに紐付く全てのリレーションをマージ
      relationsMap.values().stream()
          .flatMap(relations -> relations.stream())
          .filter(relation -> relation.getDistinctColumns().contains(column))
          .forEach(
              relation -> {
                Set<ColumnPair> pairs = relation.getColumnPairs();
                for (RelationDef relationDef : relationsInCol) {
                  relationDef.addAllPairs(
                      pairs.stream()
                          .filter(relationDef::containsAnyInPair)
                          .collect(Collectors.toList()));
                }
              });
    }
  }

  void addRelationalColumns(ColumnDef col1, ColumnDef col2) {
    ColumnPair pair = new ColumnPair(col1, col2);
    addRelations(List.of(new RelationDef(pair)));
  }
}
