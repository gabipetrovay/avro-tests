# Steps

1. Clone this repo:

```bash
git clone git@github.com:gabipetrovay/avro-tests.git
```

2. Navigate to the repo directory:

```bash
cd avro-tests
```

3. Start the Docker containers:

```bash
docker-compose up -d
```

4. Run the test

```bash
mvn test
```

or the following to remove the verbose Kafka producer and consumer logging:

```bash
mvn test 2> /dev/null
```

# All in one

```bash
git clone git@github.com:gabipetrovay/avro-tests.git
cd avro-tests
docker-compose up -d
mvn test 2> /dev/null
```
