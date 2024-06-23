import requests
import json
from openai import OpenAI

# Configuración del cliente de OpenAI con la clave API
client = OpenAI(
    api_key=''
)

# Nombre del modelo preentrenado de OpenAI
MODEL_ENGINE = "gpt-3.5-turbo"  # Ejemplo con GPT-3.5


def train_model(json_data_url):
    """
    Función para entrenar un modelo de OpenAI utilizando un archivo JSONL descargado de una URL.

    Args:
        json_data_url (str): URL del archivo JSONL con los datos de entrenamiento.

    Returns:
        str: JSON string indicando el estado del entrenamiento.
    """
    result = {
        "code": 1,
        "status": "ERROR",
        "message": "An error occurred",
        "data": []
    }

    print(f"URL del archivo JSONL: {json_data_url}")

    # Obtener el nombre del archivo local a partir de la URL
    local_filename = json_data_url.split('/')[-1]
    print(f"Nombre del archivo local: {local_filename}")

    # Descargar el archivo desde la URL proporcionada
    response = requests.get(json_data_url)
    if response.status_code == 200:
        with open(local_filename, 'wb') as local_file:
            local_file.write(response.content)
        print(f"Archivo descargado: {local_filename}")
    else:
        result["message"] = f"Error al descargar el archivo: {response.status_code}"
        return json.dumps(result)

    # Verificar el contenido del archivo descargado
    print("Contenido del archivo descargado:")
    with open(local_filename, 'r', encoding='utf-8') as local_file:
        for line in local_file:
            print(line.strip())

    # Subir el archivo descargado a OpenAI para el fine-tuning
    try:
        uploaded_file = client.files.create(
            file=open(local_filename, "rb"),
            purpose="fine-tune"
        )

        # Iniciar el proceso de fine-tuning con el archivo subido
        #fine_tuning_details = client.fine_tuning.jobs.create(
        #    training_file=uploaded_file.id,
        #    model=MODEL_ENGINE
        #)

        # Obtener los detalles del trabajo de fine-tuning
        #fine_tuning_details = client.fine_tuning.jobs.retrieve(fine_tuning_response.id)

        # ftjob-oy92QjuL8JfypKkvDRSlOg0r
        fine_tuning_details = client.fine_tuning.jobs.retrieve("ftjob-oy92QjuL8JfypKkvDRSlOg0r");

        # Construyendo el diccionario
        fine_tuning_response_dict = {
            "id": fine_tuning_details.id,
            "created_at": fine_tuning_details.created_at,
            "error": {
                "code": fine_tuning_details.error.code,
                "message": fine_tuning_details.error.message,
                "param": fine_tuning_details.error.param
            },
            "fine_tuned_model": fine_tuning_details.fine_tuned_model,
            "finished_at": fine_tuning_details.finished_at,
            "hyperparameters": {
                "n_epochs": fine_tuning_details.hyperparameters.n_epochs,
                "batch_size": fine_tuning_details.hyperparameters.batch_size,
                "learning_rate_multiplier": fine_tuning_details.hyperparameters.learning_rate_multiplier
            },
            "model": fine_tuning_details.model,
            "object": fine_tuning_details.object,
            "organization_id": fine_tuning_details.organization_id,
            "result_files": fine_tuning_details.result_files,
            "seed": fine_tuning_details.seed,
            "status": fine_tuning_details.status,
            "trained_tokens": fine_tuning_details.trained_tokens,
            "training_file": fine_tuning_details.training_file,
            "validation_file": fine_tuning_details.validation_file,
            "estimated_finish": fine_tuning_details.estimated_finish,
            "integrations": fine_tuning_details.integrations,
            "user_provided_suffix": fine_tuning_details.user_provided_suffix
        }

        print(f"fine_tuning_details: {fine_tuning_details}")
        # Imprimir los detalles para verificar
        print(f"Fine-tuning details: {fine_tuning_response_dict}")

        result["code"] = 0
        result["status"] = "OK"
        result["message"] = "Transaction successfully completed"
        result["data"].append(fine_tuning_response_dict)

        print(f"Fine-tuning iniciado: {fine_tuning_response_dict}")
    except Exception as e:
        result["message"] = f"Error al subir el archivo o iniciar el fine-tuning: {e}"
        print(result["message"])

    return json.dumps(result)
