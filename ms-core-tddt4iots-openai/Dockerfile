# Usamos Amazon Corretto 22 como base para la ejecución (Runtime)
FROM amazoncorretto:22-alpine

# Directorio de trabajo
WORKDIR /app

# Copiamos el archivo JAR generado localmente desde la carpeta target
# Usamos un comodín para asegurar que encuentre el archivo sin importar el nombre exacto
COPY target/*.jar ms-core-tddt4iots-openai-1.0.0.jar

# Exponemos el puerto que definiste (8181)
EXPOSE 8181

# Comando para iniciar la aplicación con parámetros de optimización de memoria si fuera necesario
ENTRYPOINT ["java", "-jar", "ms-core-tddt4iots-openai-1.0.0.jar"]