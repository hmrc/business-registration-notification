# business-registration-notification.

## Running locally

`sbt run` or `./run.sh` for testing

### Testing

#### [ScalaTest](https://www.scalatest.org/)

run unit test suite with

```bash
sbt test
```

run integration test suit execute

```bash
sbt it/test
```

#### [Scoverage](https://github.com/scoverage/sbt-scoverage)

```bash
sbt clean coverage test it/test coverageReport
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")


