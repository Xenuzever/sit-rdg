package io.sitoolkit.rdg.core;

import static com.google.common.truth.Truth.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.junit.Before;
import org.junit.Test;

public class MainTest {

  Path resources;
  Path input = Path.of("target/input");
  Path output = Path.of("target/output");

  @Before
  public void before() throws IOException, URISyntaxException {
    resources = Paths.get(Main.class.getClassLoader().getResource(".").toURI());
    Files.createDirectories(input);
    Files.createDirectories(output);

    Files.copy(
        resources.resolve("generator-config.json"),
        input.resolve("generator-config.json"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.copy(
        resources.resolve("multiple-create.sql"),
        input.resolve("multiple-create.sql"),
        StandardCopyOption.REPLACE_EXISTING);
    Files.copy(
        resources.resolve("relational-columns.sql"),
        input.resolve("relational-columns.sql"),
        StandardCopyOption.REPLACE_EXISTING);
  }

  @Test
  public void execute() {
    int exitVal =
        new Main()
            .execute(
                new String[] {
                  "read-sql", "gen-data", "--input", input.toString(), "--output", output.toString()
                });

    assertThat(exitVal).isEqualTo(0);

    assertThat(Files.exists(input.resolve("schema.json"))).isEqualTo(true);
    assertThat(Files.exists(output.resolve("UNKNOWN.CAMEL_CASE_TABLE.csv"))).isEqualTo(true);
    assertThat(Files.exists(output.resolve("UNKNOWN.SNAKE_CASE_TABLE.csv"))).isEqualTo(true);

    assertThat(Files.exists(output.resolve("UNKNOWN.DOUBLE_QUOTED_TABLE.csv"))).isEqualTo(true);
  }
}
