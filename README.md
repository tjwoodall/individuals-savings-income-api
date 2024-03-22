
# individuals-savings-income-api

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)


The Individuals Savings Income API allows a developer to create, amend, retrieve and delete data relating to Savings Income.

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager V2](https://github.com/hmrc/sm2)

## Development Setup

Run from the console using: `sbt run` (starts on port 9770 by default)

Start the service manager profile: `sm2 --start MTDFB_INDIVIDUALS_SAVINGS_INCOME`

## Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

Note: if you run into `java.lang.OutOfMemoryError` errors, add a `.sbtopts` file to the root of the project with the
following contents:

```
-J-Xmx3G
-J-XX:+UseG1GC
```

## Viewing OAS

To view documentation locally ensure the Individuals Savings Income API is running, and run api-documentation-frontend:

```
./run_local_with_dependencies.sh
```

Then go to http://localhost:9680/api-documentation/docs/openapi/preview and enter the full URL path to the YAML file with the
appropriate port and version:

```
http://localhost:9770/api/conf/1.0/application.yaml
```

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [Individuals Savings Income Documentation](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/individuals-savings-income-api)

## License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")