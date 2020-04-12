package io.sitoolkit.rdg.core.domain.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.sitoolkit.rdg.core.infrastructure.JsonUtils;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaInfo {

  private static final String FILE_NAME = "schema.json";

  private Set<SchemaDef> schemas = new HashSet<>();

  @JsonIgnore
  public Optional<SchemaDef> findByName(String schemaName) {
    return schemas.stream().filter(schema -> schema.equalsName(schemaName)).findFirst();
  }

  @JsonIgnore
  public Optional<TableDef> findTable(String schemaName, String tableName) {
    Optional<SchemaDef> schemaOpt = findByName(schemaName);

    if (!schemaOpt.isPresent()) {
      return Optional.empty();
    }

    return schemaOpt.get().findTable(tableName);
  }

  public Optional<ColumnDef> findColumn(String schemaName, String tableName, String columnName) {
    Optional<TableDef> tableOpt = findTable(schemaName, tableName);

    if (!tableOpt.isPresent()) {
      return Optional.empty();
    }

    return tableOpt.get().findColumn(columnName);
  }

  public static SchemaInfo read(Path inDir) {
    SchemaInfo schemaInfo = JsonUtils.json2object(inDir.resolve(FILE_NAME), SchemaInfo.class);
    schemaInfo.afterDeserialize();
    return schemaInfo;
  }

  public Path write(Path outDir) {
    return JsonUtils.object2jsonFile(this, outDir.resolve(FILE_NAME));
  }

  public void afterDeserialize() {
    schemas.stream().forEach(SchemaDef::afterDeserialize);
  }

  @JsonIgnore
  public List<RelationDef> getAllRelations() {
    return schemas.stream()
        .map(SchemaDef::getRelations)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public List<TableDef> getAllTables() {
    return schemas.stream()
        .map(SchemaDef::getTables)
        .flatMap(Set::stream)
        .collect(Collectors.toList());
  }

  public void add(String schemaName, TableDef table) {
    String defaultSchemaName = StringUtils.defaultString(schemaName);

    SchemaDef schema =
        findByName(schemaName)
            .orElseGet(
                () -> {
                  SchemaDef newSchema = new SchemaDef();
                  newSchema.setName(defaultSchemaName);
                  schemas.add(newSchema);
                  return newSchema;
                });

    schema.add(table);
  }
}
