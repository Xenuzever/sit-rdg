package io.sitoolkit.rdg.core;

import io.sitoolkit.rdg.core.application.DataGenerator;
import io.sitoolkit.rdg.core.application.DataGeneratorRelationFirstImpl;
import io.sitoolkit.rdg.core.application.SchemaAnalyzer;
import io.sitoolkit.rdg.core.infrastructure.ResourceUtils;
import io.sitoolkit.rdg.core.infrastructure.RuntimeOptions;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.math.NumberUtils;

@Slf4j
public class Main {

  static Option inputOpt =
      Option.builder("i")
          .argName("InputDirectory")
          .desc("Path of input directory (default ./input)")
          .longOpt("input")
          .required(false)
          .hasArg()
          .build();

  static Option outputOpt =
      Option.builder("o")
          .argName("OutputDirectory")
          .desc("Path of Output directroy (default ./output)")
          .longOpt("output")
          .required(false)
          .hasArg()
          .build();

  static Option bufferSizeOpt =
      Option.builder("b")
          .argName("BuffreSize")
          .desc("Buffre size of csv row count (default 1000)")
          .longOpt("buffer-size")
          .required(false)
          .hasArg()
          .build();

  static Option flushWaitAlertSecOpt =
      Option.builder("fat")
          .argName("FlushAlertTheshold")
          .desc("Threshold to alert of wait span for csv writing (default 10 (sec))")
          .longOpt("flush-alert-threahold")
          .required(false)
          .hasArg()
          .build();

  static Options options =
      new Options()
          .addOption(inputOpt)
          .addOption(outputOpt)
          .addOption(bufferSizeOpt)
          .addOption(flushWaitAlertSecOpt);

  SchemaAnalyzer schemaAnalyzer = new SchemaAnalyzer();

  DataGenerator dataGenerator = new DataGeneratorRelationFirstImpl();

  public static void main(String[] args) {
    System.exit(new Main().execute(args));
  }

  public int execute(String[] args) {

    CommandLineParser parser = new DefaultParser();

    if (args.length == 0) {
      printHelp();
    }

    CommandLine cmd;
    try {
      cmd = parser.parse(options, args);

      return execute(cmd);
    } catch (ParseException e) {
      log.error("Error:", e);
      printHelp();
      return 1;
    }
  }

  public int execute(CommandLine cmd) {

    try {

      Path input =
          Paths.get(cmd.getOptionValue(inputOpt.getLongOpt(), "input"))
              .toAbsolutePath()
              .normalize();

      String output = cmd.getOptionValue(outputOpt.getLongOpt(), "output");
      List<Path> outDirs =
          Arrays.asList(output.split(",")).stream()
              .map(Path::of)
              .map(Path::toAbsolutePath)
              .map(Path::normalize)
              .collect(Collectors.toList());

      String bufferSize = cmd.getOptionValue(bufferSizeOpt.getLongOpt());
      RuntimeOptions.getInstance().setBufferSize(NumberUtils.toInt(bufferSize, 1000));

      String flushWaitAlertSec = cmd.getOptionValue(flushWaitAlertSecOpt.getLongOpt());
      RuntimeOptions.getInstance().setFlushWaitAlertSec(NumberUtils.toInt(flushWaitAlertSec, 10));

      if (cmd.getArgList().contains("read-sql")) {
        Path out = schemaAnalyzer.analyze(input);
        log.info("json: {}", out);
      }

      if (cmd.getArgList().contains("gen-data")) {
        List<Path> outputs = dataGenerator.generate(input, outDirs);
        outputs.forEach(out -> log.info("csv: {}", out));
      }

      return 0;

    } catch (Exception e) {
      log.error("Error: ", e);
      return 1;
    }
  }

  void printHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(-1);

    String jarFileName =
        Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
            .getFileName()
            .toString();

    String header = ResourceUtils.res2str("help.txt");
    formatter.printHelp("java -jar " + jarFileName + " [COMMAND...]", header, options, "", true);
  }
}
