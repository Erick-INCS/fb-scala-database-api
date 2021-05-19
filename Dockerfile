FROM mozilla/sbt

RUN git clone https://github.com/Erick-INCS/scala-database-inserter-ws.git
WORKDIR /scala-database-inserter-ws
RUN sed -e s/"localhost\/3050:\/firebird\/data\/testdb.fdb"/"\/grsc\/Clientes\/ClientesGRSA\/Rockwell Automation Monterrey\/Base de datos 2021\/Migracion de BD\/ROCKWELL_MIG.FDB"/g app.conf > tmp.conf
RUN mv tmp.conf app.conf
RUN sbt clean && sbt compile

CMD ["sbt", "run"]
