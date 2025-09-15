FROM openjdk:17
 
WORKDIR /usrapp/bin
 
ENV PORT=6000
 
COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency
 COPY src/main/resources/ /usrapp/bin/classes/
 
CMD ["java","-cp","./classes:./dependency/*","edu.eci.arep.docker.RestServiceApplication"]
