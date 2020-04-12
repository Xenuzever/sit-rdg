package io.sitoolkit.rdg.core.domain.schema;

import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

@Slf4j
public class TableDependencyComparator implements Comparator<TableDef> {

  @Override
  public int compare(TableDef o1, TableDef o2) {
    int ret = o2.compareTo(o1);
    if (!o1.equals(o2)) {
      if (o1.dependsOn(o2)) {
        ret = 100;
      } else if (o2.dependsOn(o1)) {
        ret = -1;
      }
    }

    if (log.isTraceEnabled()) {
      log.trace("{} vs {} = {}", o1.getName(), o2.getName(), ret);
    }

    return ret;
  }

  public static void main(String[] args) {
    for (int i = 0; i < 100; i++) {
      System.out.println(RandomStringUtils.random(10).compareTo(RandomStringUtils.random(10)));
    }
  }
}
