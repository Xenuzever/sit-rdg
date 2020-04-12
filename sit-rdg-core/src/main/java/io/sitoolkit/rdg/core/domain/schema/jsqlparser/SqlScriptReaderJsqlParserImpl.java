package io.sitoolkit.rdg.core.domain.schema.jsqlparser;

import io.sitoolkit.rdg.core.domain.schema.SchemaInfo;
import io.sitoolkit.rdg.core.domain.schema.SqlScriptReader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;

/** SqlScriptReaderJsqlParserImpl */
@Slf4j
public class SqlScriptReaderJsqlParserImpl implements SqlScriptReader {

  private SchemaInfoStore store = new SchemaInfoStore();

  @Getter private SchemaInfo schemaInfo = new SchemaInfo();

  @Override
  public void read(String sqlText) {

    DynamicRelationFinder dynamicRelFinder = new DynamicRelationFinder(store);
    StaticRelationFinder staticRelFinder = new StaticRelationFinder(schemaInfo);

    try {
      Statements stmts = CCJSqlParserUtil.parseStatements(sqlText);

      stmts.accept(staticRelFinder);

      stmts.accept(
          new StatementVisitorAdapter() {

            @Override
            public void visit(Select select) {
              select.getSelectBody().accept(dynamicRelFinder);
            }
          });

    } catch (JSQLParserException e) {
      log.error("Error on parsing", e);
    }
  }
}
