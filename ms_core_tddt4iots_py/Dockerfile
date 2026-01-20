# Usamos una imagen de Python ligera
FROM python:3.9-slim

# Evitar que Python genere archivos .pyc y permitir logs en tiempo real
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

WORKDIR /app

# Instalamos dependencias del sistema necesarias para gRPC (opcional pero recomendado)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Copiamos el archivo de requerimientos
COPY requirements.txt .

# Instalamos las librerías de Python
RUN pip install --no-cache-dir -r requirements.txt

# Copiamos todo el código del proyecto
COPY . .

ENV PYTHONPATH=/app

# Exponemos el puerto por defecto de gRPC
EXPOSE 50051

# Comando para arrancar el servidor
# Ajusta el nombre a tu archivo principal
CMD ["python", "-m", "grpc_service.grpc_server"]