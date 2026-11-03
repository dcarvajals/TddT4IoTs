# Metodología de investigación para generación UML

## Objetivo experimental

Evaluar la transformación de requisitos completos en lenguaje natural hacia un workspace CASE coherente con varios diagramas de casos de uso, un modelo de clases global y una secuencia por caso de uso.

La unidad experimental es un **proyecto**, nunca una frase ni un paso aislado. Los documentos usados en producción son entradas; solo la versión corregida y aprobada por especialistas puede convertirse en dato de entrenamiento.

## Control del dataset

- Anonimizar datos personales, secretos y nombres de organizaciones.
- Revisar cada `goldWorkspace` por al menos dos especialistas y adjudicar desacuerdos.
- Dividir por proyecto en `TRAIN`, `VALIDATION` y `TEST`.
- Congelar el test antes de diseñar prompts o entrenamientos.
- Guardar manifiesto, SHA-256, versión del schema y commit del código.
- Evitar que revisiones del mismo proyecto aparezcan en splits diferentes.

## Experimentos

| Código | Configuración |
|---|---|
| E0 | Sistema anterior como baseline |
| E1 | Modelo base y prompt simple |
| E2 | Few-shot y salida estructurada estricta |
| E3 | Extracción de requisitos, síntesis UML y validación determinista |
| E4 | E3 más fine-tuning supervisado, cuando esté disponible |
| E5 | Ablaciones: sin schema, ejemplos, etapas o validadores |

Cada método utiliza los mismos proyectos de prueba. Se recomienda repetir cada generación entre tres y cinco veces para medir estabilidad.

## Métricas atómicas

### Estructura

- `json_valid`: 0 o 1.
- `schema_valid`: 0 o 1.
- `referential_integrity`: referencias válidas / referencias totales.
- `generation_success`: importación sin intervención, 0 o 1.
- `repair_count` y `retry_count`.

### Casos de uso

- Precisión, recall y F1 de actores y casos de uso.
- Cobertura de requisitos: requisitos trazados / requisitos gold.
- Cobertura de flujo principal y alternativo.
- Tasa de alucinación: elementos sin soporte / elementos generados.

### Clases

- F1 de clases, atributos, métodos y relaciones.
- Exactitud del tipo de relación y multiplicidad.
- F1 de nodos/aristas y distancia normalizada de edición del grafo.

### Secuencias

- F1 de participantes y mensajes.
- Exactitud del orden, usando LCS o Kendall tau según la representación.
- Mensajes vinculados a métodos válidos / mensajes totales.
- Pasos de caso de uso representados / pasos totales.

### Consistencia y operación

- Secuencias existentes / casos de uso.
- Clases justificadas / clases generadas.
- Trazas completas requisito-caso-clase-método / trazas esperadas.
- Latencia p50/p95, tokens, coste, reintentos y correcciones humanas.

## Análisis estadístico

- Informar media, mediana, desviación estándar e IQR.
- Calcular intervalos bootstrap del 95% para F1 y métricas continuas.
- Usar Wilcoxon pareado entre dos métodos si no hay normalidad; t pareado si se justifican sus supuestos.
- Usar Friedman para más de dos métodos y corrección de Holm en pruebas post-hoc.
- Usar McNemar para resultados binarios como schema válido o inválido.
- Informar tamaño de efecto: Cohen's d o Cliff's delta.
- Medir acuerdo humano con kappa de Cohen para dos evaluadores o alfa de Krippendorff para más evaluadores/datos incompletos.

No deben publicarse únicamente promedios agregados: `ai_metric_result` conserva la observación por ejecución para permitir pruebas pareadas y auditoría.
