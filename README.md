# hnsw-scala

HNSW algorithm implementations

## Implementations

| implementation | description                                                            | path                                                                        |
|----------------|------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| ref            | the reference implementation from https://arxiv.org/pdf/1603.09320.pdf | [RefHnsw.scala](core/src/main/scala/io/github/tuannh982/hnsw/RefHnsw.scala) |
|                |                                                                        |                                                                             |

## Parameters tuning

See [ExampleIntVecL2DF.scala](core%2Fsrc%2Fmain%2Fscala%2Fio%2Fgithub%2Ftuannh982%2Fhnsw%2Ftuning%2FExampleIntVecL2DF.scala)
for tuning example

## Benchmarks

### MacBook Pro, Apple M1 Pro 16GB RAM)

```shell
bin/sbt "bench/Jmh/run -i 10 -wi 2 -f1 -t10 -rf text"
```

```text
Benchmark                                   Mode  Cnt     Score     Error  Units
RefHnswBench100000Vectors5Neighbors.query  thrpt   10  8483.214 ± 240.390  ops/s
```