FROM node:21.4.0

# ---------------------------------------------------------
# 📁 Directorio de trabajo
# ---------------------------------------------------------
WORKDIR /app

# Copiar package.json
COPY package*.json ./

# Instalar dependencias NPM
RUN npm install

# Copiar código fuente
COPY . .

# Puerto del backend
EXPOSE 3001

# Comando de arranque
CMD ["npm", "start"]
