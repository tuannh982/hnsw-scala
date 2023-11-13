# hnsw-scala

HNSW algorithm implementations

## Implementations

| model    | description                                                            | path                                                                |
|----------|------------------------------------------------------------------------|---------------------------------------------------------------------|
| baseline | the base implementation from from https://arxiv.org/pdf/1603.09320.pdf | [core/src/main/scala/io/github/tuannh982/hnsw/BaselineHnsw.scala](core/src/main/scala/io/github/tuannh982/hnsw/BaselineHnsw.scala) |
|          |                                                                        |                                                                     |

## Benchmarks

Command

```shell
bin/sbt "bench/Jmh/run -i 8 -wi 2 -f1 -t8 -rf text"
```

Results

```text
Benchmark                                        Mode  Cnt     Score      Error  Units
BaselineHnswBench100000Vectors5Neighbors.query  thrpt    8  7125.138 ± 1138.292  ops/s
```