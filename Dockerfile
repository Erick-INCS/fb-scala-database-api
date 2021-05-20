FROM mozilla/sbt

# RUN git clone https://github.com/Erick-INCS/scala-database-inserter-ws.git
# WORKDIR /scala-database-inserter-ws
# RUN sed -e s/"localhost\/3050:\/firebird\/data\/testdb.fdb"/"\/grsc\/Clientes\/ClientesGRSA\/Rockwell Automation Monterrey\/Base de datos 2021\/Migracion de BD\/ROCKWELL_MIG.FDB"/g app.conf > tmp.conf
# RUN mv tmp.conf app.conf

# From https://adoptopenjdk.net/
RUN wget https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.11%2B9/OpenJDK11U-jdk_x64_linux_hotspot_11.0.11_9.tar.gz
RUN tar -xzvf OpenJDK11U-jdk_x64_linux_hotspot_11.0.11_9.tar.gz
RUN rm OpenJDK11U-jdk_x64_linux_hotspot_11.0.11_9.tar.gz && mv jdk-11.0.11+9 /usr/local/

ENV JAVA_VERSION=11u11
ENV JAVA_HOME=/usr/local/jdk-11.0.11+9
ENV PATH="/usr/local/jdk-11.0.11+9/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

COPY . /app
WORKDIR /app
RUN sbt clean && sbt compile

CMD ["sbt", "run"]
