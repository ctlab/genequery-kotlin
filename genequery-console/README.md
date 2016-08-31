## Quick Start
You need to have Java8 to be pre-installed on your machine. Check it first:
```
java -version
```
If you see `java version "1.8.<some-version>"`, go ahead:
```
wget http://genome.ifmo.ru/genequery-media/2015/downloads/console/gqcmd-internal.tar.gz
wget http://genome.ifmo.ru/genequery-media/2015/downloads/console/gqcmd.jar
tar -zxf gqcmd-internal.tar.gz
java -jar gqcmd.jar -h
```

## Building

`../gradlew build` from `genequery-console` project folde or

`./gradlew :console:build` from `genequery-kotlin` folder.

## Internal data
Check out [donwloads](http://genome.ifmo.ru/genequery/downloads) page on GeneQuery web site.
You will need to decompress the archive and specify path to the folder as gqcmd option.

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
java -jar path/to/gqcmd.jar bulkquery -t 6 -dp /path/to/gqcmd-internal -q /path/to/queries -d /path/to/result
~ tree path/to/result
path/to/result
├── 1.txt.conversion.csv
├── 1.txt.result.csv
└── 2.txt.err
```
