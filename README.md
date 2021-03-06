Individuals Reliefs API
========================

The Individuals Reliefs API allows a developer to retrieve, create and amend, and delete:
- Relief Investments
- Other Reliefs
- Foreign Reliefs
- Pension Reliefs

## Requirements

- Scala 2.12.x
- Java 8
- sbt > 1.3.7
- [Service Manager](https://github.com/hmrc/service-manager)

## Running the microservice

Run from the console using: `sbt run` (starts on port 7796 by default)

Start the service manager profile: `sm --start MTDFB_INDIVIDUALS_RELIEFS`

## Running test

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

## Viewing RAML

To view documentation locally ensure the Obligations API is running, and run api-documentation-frontend:
```
./run_local_with_dependencies.sh
```
Then go to http://localhost:9680/api-documentation/docs/api/preview and use this port and version:
```
http://localhost:7796/api/conf/1.0/application.raml
```

## Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/individuals-reliefs-api/issues)

## API Reference / Documentation 

Available on the [HMRC Developer Hub](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/individuals-reliefs-api/1.0)

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
