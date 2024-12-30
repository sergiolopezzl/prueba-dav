# Imagen oficial de MySQL
FROM mysql:8.0

# Variables de entorno necesarias
ENV MYSQL_ROOT_PASSWORD=rootpassword
ENV MYSQL_DATABASE=mydatabase
ENV MYSQL_USER=myuser
ENV MYSQL_PASSWORD=mypassword

# Copiar un script SQL para inicializar la base de datos
COPY init.sql /docker-entrypoint-initdb.d/

# Exponer el puerto de MySQL
EXPOSE 3306

#docker stop my-mysql-container
#docker rm my-mysql-container

#docker build -t my-mysql-image .
#docker run -d --name my-mysql-container -p 3306:3306 my-mysql-image
#docker start my-mysql-container

#mvn clean install
#mvn exec:java '-Dexec.mainClass=app.apiRESTful.App'



