package io.sitoolkit.rdg.core.domain.generator.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class ValueGeneratorSpecDeserializer extends JsonDeserializer<ValueGenerator> {

  @Override
  public ValueGenerator deserialize(JsonParser p, DeserializationContext ctx) throws IOException {

    ObjectMapper mapper = (ObjectMapper) p.getCodec();
    JsonNode node = mapper.readTree(p);

    String type = node.get("type").textValue();

    Class<? extends ValueGenerator> generatorType = null;

    switch (type) {
      case "sequence":
        generatorType = SequenceValueGenerator.class;
        break;
      case "multi-sequence":
        generatorType = MultiSequenceValueGenerator.class;
        break;
      case "choice":
        generatorType = ChoiceValueGenerator.class;
        break;
      case "date":
        generatorType = DateValueGenerator.class;
        break;
      case "range":
        generatorType = RangeValueGenerator.class;
        break;
      default:
        generatorType = RandomValueGenerator.class;
    }

    ValueGenerator generator = mapper.readValue(node.toString(), generatorType);

    generator.initialize();

    return generator;
  }
}
