const express = require('express');
const app = express();
const armadillo = require('./library/armadillo');

// Middleware para parsear el cuerpo de las solicitudes como JSON
app.use(express.json());

// Configurar un servicio POST para interpretar texto
app.post('/api/armadillo', (req, res) => {
    console.log(req.body); // Log para depurar el contenido del cuerpo
    const { text } = req.body;
    console.log(text)
    const resultado = armadillo(text);
    const data = {
        diagram: resultado[1].diagram,
        relationships: resultado[1].relationships
    };
    const response = {
        classDiagram: JSON.stringify(data),
        textInterpret: resultado[0]
    }
    res.json(response);
});

app.listen(3001, () => {
    console.log('Server is running on port 3000');
})
