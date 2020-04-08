# Random Data Generator

Random Data Generator (sit-rdg) is a tool to generate data set for TEST.
sit-rdg analyzes DDL and DML script file and resolve DB schema from them, i.e. tables, columns, relations, and generate csv files per table.


## Required Software

* Java
* Maven (Optional)


## How to Use

1. Download sit-rdg-xxx.jar or put pom.xml in your workspace directory.
2. Put DDL and DML scripts used in your application in the input directory. (input/*.sql)
3. Execute sit-rdg.
4. Then csv files are generated in the output directory. (output/*.csv)

```
- workspace
    sit-rdg-xxx.jar          |
    pom.xml                  |-- 1.

  - input
      create-table_1.sql     |
      create-table_2.sql     |
      select-some.sql        |-- 2.
        :

      schema.json  
      generator-config.json

  - output
      table_1.csv            |
      table_2.csv            |-- 4.
        :
```

You can specify the behavior of sit-rdg in detail using following 2 files.

* **schema.json**
  * Metadata of DB schema which is the result of analysis of *.sql
  * This files are generated by sit-rdg, but you can modify it or create from scratch. 
* **generator-config.json**
  * Specification of data generation


You can execute sit-rtg with the following 2 ways.

* Java Command
* Maven Plugin

### Java Command

Exeucte the following commands in your workspace.

```bash
curl -o sit-rdg-core-1.0.0-beta.1-SNAPSHOT.jar https://repo.maven.apache.org/maven2/io/sitoolkit/rdg/sit-rdg-core/1.0.0-beta.1-SNAPSHOT/sit-rdg-core-1.0.0-beta.1-SNAPSHOT.jar

java -jar sit-rdg-core-1.0.0-beta.1-SNAPSHOT.jar
```


### Maven

Put pom.xml in your workspace and execute mvn command.

* pom.xml

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>your-group-id</groupId>
  <artifactId>your-artifact-id</artifactId>
  <version>1.0.0-beta.1-SNAPSHOT</version>

  <build>

    <defaultGoal>sit-rdg:run</defaultGoal>

    <plugins>
      <plugin>
        <groupId>io.sitoolkit.rdg</groupId>
        <artifactId>sit-rdg-maven-plugin</artifactId>
        <version>1.0.0-beta.1-SNAPSHOT</version>
      </plugin>
    </plugins>

  </build>

</project>
```

```
mvn
```

## generator-config.json



```js
{
  "scale": "1/10",
  "defaultRowCount": 100,
  "defaultRequiredValueCount": 100,
  "schemaConfigs": [
    {
      "schemaName": "",
      "tableConfigs": [
        {
          "tableName": "TABLE_1",
          "priorityRank": 1,
          "columnConfigs": [
            {
              "columnName": "COLUMN_1",
              "requiredValueCount": 1,

            　// See -Data Generation Specification-
              "spec": {
                "type": "sequence",
                "start": 0,
                "end": 9,
                "step": 1
              } 
            }
          ]
        }
      ]
    }
  ]
}
```

### Data Generation Specification


#### sequence

Spec: sequence is to generate sequencial value per generation.

```js
{
  "type": "sequence",
  "start": 0,  // start value of sequence (default 0)
  "end": 9,    // end value of sequence (defalut Long.MAX_VALUE)
  "step": 1    // increment value per generation of sequence (default 1)
               // if sequence value is over the end value, the value is reset to start
}
```

#### range

Spec: range is to generate values between specified range.

```js
{
  "type": "range",
  "ranges": [
    // this range generates 0, 1, 2 with 40% probability
    {
      "min": 0,
      "max": 2,
      "step": 1,
      "ratio": 0.2
    },
    // this range generates 3, 6, 9 with 60% probability
    {
      "min": 3,
      "max": 9,
      "step": 3,
      "ratio": 0.3
    }
  ]
}
```

```json
{
  "type": "choice",
  "values": [
    {
      "value": "01",
      "ratio": 0.3
    },
    {
      "value": "02",
      "ratio": 0.2
    }
  ]
}
```
