FROM incspn/scala-sbt

COPY . /app
WORKDIR /app
RUN sbt clean && sbt compile

# RUN sed -e s/"localhost\/3050:\/firebird\/data\/testdb.fdb"/"\/grsc\/Clientes\/ClientesGRSA\/Rockwell Automation Monterrey\/Base de datos 2021\/Migracion de BD\/ROCKWELL_MIG.FDB"/g app.conf > tmp.conf
# RUN mv tmp.conf app.conf
 
CMD ["sbt", "run"]
