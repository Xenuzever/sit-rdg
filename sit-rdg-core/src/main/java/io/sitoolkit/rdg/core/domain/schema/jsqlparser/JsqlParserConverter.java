package io.sitoolkit.rdg.core.domain.schema.jsqlparser;

import io.sitoolkit.rdg.core.domain.schema.ColumnDef;
import io.sitoolkit.rdg.core.domain.schema.ConstraintAttribute;
import io.sitoolkit.rdg.core.domain.schema.DataType;
import io.sitoolkit.rdg.core.domain.schema.DataTypeName;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsqlParserConverter {

  public static TableDef convert(CreateTable createTable) {

    TableDef tableDef = convert(createTable.getTable());

    List<ColumnDef> columns =
        createTable.getColumnDefinitions().stream()
            .map(JsqlParserConverter::convert)
            .peek(
                c -> {
                  List<ConstraintAttribute> constraints =
                      ConstraintAttribute.matchedConstraints(createTable.getIndexes(), c);
                  c.setConstraints(constraints);
                  c.setTable(tableDef);
                })
            .collect(Collectors.toList());

    tableDef.setColumns(columns);

    return tableDef;
  }

  public static TableDef convert(Table table) {

    String schemaName = table.getSchemaName();
    String name = table.getName();
    String alias = name;
    if (Objects.nonNull(table.getAlias())) {
      alias = table.getAlias().getName();
    }

    TableDef tableDef = TableDef.builder().name(name).columns(new ArrayList<>()).build();

    return tableDef;
  }

  public static ColumnDef convert(ColumnDefinition columnDefinition) {

    String name = columnDefinition.getColumnName();
    DataTypeName dataTypeName = DataTypeName.parse(columnDefinition.getColDataType().getDataType());
    DataType dataType = DataType.builder().name(dataTypeName).build();
    dataTypeName.resolve(columnDefinition.getColDataType().getArgumentsStringList(), dataType);

    return ColumnDef.builder().name(name).dataType(dataType).build();
  }
}
