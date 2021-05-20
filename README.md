# Scala Remote Database API

### Configure
You can configure the application setting the followin eviroment variables:
- `SAPI_DB_USER`
- `SAPI_DB_PASS`
- `SAPI_DB_URL`

Alternatively you can setup the desired configuration in the `app.conf` file as follows:
```
user=sysdba
password=masterkey
url=localhost/3050:/firebird/data/testdb.fdb
```

Create the output directories `out`, `success` and `errors`:
`mkdir -p out success errors`

### Start application
#### With sbt
`sbt run`

#### With Docker
```bash
docker build . -t scala-fb-api

# Example run command
docker run --rm -it --net=host scala-fb-api
```

### Test the API
```bash
curl --header "Content-Type: application/json" --request POST --data '{"sql": "SELECT * FROM TESTTABLE;"}' localhost:8080/sql > result.csv

cat result.csv
```
```csv
ID,HASH
-25,hashvalue01
-24,hashvalue02
-22,hashvalue03
-21,hashvalue04
```
