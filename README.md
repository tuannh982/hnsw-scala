# hnsw-scala

HNSW algorithm implementations

## Implementations

| implementation | description                                                            | path                                                                             |
|----------------|------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| ref            | the reference implementation from https://arxiv.org/pdf/1603.09320.pdf | [RefHnsw.scala](core/src/main/scala/io/github/tuannh982/hnsw/RefHnsw.scala) |
|                |                                                                        |                                                                                  |

## Benchmarks

Command

```shell
bin/sbt "bench/Jmh/run -i 8 -wi 2 -f1 -t8 -rf text"
```

Results

```text
TODO
```