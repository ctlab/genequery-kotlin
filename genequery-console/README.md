## Building

`../gradlew build` from `genequery-console` project folde or

`./gradlew :console:build` from `genequery-kotlin` folder.

## Internal data
Ask @smolcoder how to get the data.

## Commands

`java -jar path/to/gqcmd.jar <COMMAND_NAME> [command arguments]`

Try `java -jar path/to/gqcmd.jar -h`.

### `bulkquery` example

Try `java -jar path/to/gqcmd.jar bulkquery -h` first.

```
~ tree path/to/queries
path/to/queries
├── 1.txt
└── 2.txt
```

```
~ less path/to/queries/1.txt
mm
hs
Symbol1
Symbol2
Symbol3 Symbol4 Symbol5

~ less path/to/queries/2.txt
hs
not-a-species
```

```
java -jar path/to/gqcmd.jar bulkquery -t 6 -dp /path/to/genequery-data-files -q /path/to/queries -d /path/to/result
~ tree path/to/result
path/to/result
├── 1.txt.conversion.csv
├── 1.txt.result.csv
└── 2.txt.err
```
