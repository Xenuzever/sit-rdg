package io.sitoolkit.rdg.core.domain.schema.jsqlparser;

import io.sitoolkit.rdg.core.domain.schema.ColumnDef;
import io.sitoolkit.rdg.core.domain.schema.ColumnPair;
import io.sitoolkit.rdg.core.domain.schema.RelationDef;
import io.sitoolkit.rdg.core.domain.schema.SchemaInfo;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import org.apache.commons.lang3.StringUtils;

/** StaticRelationFinder */
@AllArgsConstructor
@Slf4j
public class StaticRelationFinder extends StatementVisitorAdapter {

  // private SchemaInfoStore store;

  private SchemaInfo schemaInfo;

  @Override
  public void visit(CreateTable createTable) {
    log.debug("Visit: {}", createTable);
    schemaInfo.add(
        createTable.getTable().getSchemaName(), JsqlParserConverter.convert(createTable));

    if (createTable.getIndexes() == null) {
      return;
    }

    createTable.getIndexes().stream()
        .filter(ForeignKeyIndex.class::isInstance)
        .map(ForeignKeyIndex.class::cast)
        .forEach(fk -> analyzeFk(createTable.getTable(), fk));
  }

  @Override
  public void visit(Alter alter) {
    log.debug("Visit: {}", alter);

    alter.getAlterExpressions().stream()
        .map(AlterExpression::getIndex)
        .filter(Objects::nonNull)
        .filter(ForeignKeyIndex.class::isInstance)
        .map(ForeignKeyIndex.class::cast)
        .forEach(fk -> analyzeFk(alter.getTable(), fk));
  }

  public void analyzeFk(Table table, ForeignKeyIndex fk) {
    log.debug("Analyze: {}", fk);
    String schemaName = StringUtils.defaultString(table.getSchemaName());
    Optional<TableDef> mainTableOpt = schemaInfo.findTable(schemaName, fk.getTable().getName());

    if (!mainTableOpt.isPresent()) {
      return;
    }

    String referencedTableName = fk.getTable().getName();
    String readingTableName = table.getName();
    List<ColumnPair> pairs = new ArrayList<>();

    for (int i = 0; i < fk.getColumnsNames().size(); i++) {
      ColumnDef mainColumn =
          schemaInfo
              .findColumn(
                  fk.getTable().getSchemaName(),
                  referencedTableName,
                  fk.getReferencedColumnNames().get(i))
              .orElseThrow();
      ColumnDef subColumn =
          schemaInfo
              .findColumn(table.getSchemaName(), readingTableName, fk.getColumnsNames().get(i))
              .orElseThrow();

      pairs.add(new ColumnPair(mainColumn, subColumn));
    }

    RelationDef relation = new RelationDef();
    relation.addAllPairs(pairs);

    schemaInfo.findByName(schemaName).orElseThrow().getRelations().add(relation);
  }
}
