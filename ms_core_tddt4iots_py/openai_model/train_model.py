import os
import requests
import json
from datetime import datetime, timezone
import hashlib
import time
from pathlib import Path
from openai import OpenAI, APITimeoutError
import httpx
from openai_model.case_workspace_schema import CASE_WORKSPACE_RESPONSE_FORMAT

# Keep Java's gRPC deadline longer than this HTTP timeout.
INFERENCE_TIMEOUT_SECONDS = 600

def train_model(json_string):
    """
    Función para entrenar un modelo de OpenAI utilizando un archivo JSONL descargado de una URL.

    Args:
        json_string (str): JSON en formato string con los parametros necesarios.

    Returns:
        str: JSON string indicando el estado del entrenamiento.
    """

    #print(f"URL del archivo JSONL: {json_string}")

    result = {
        "code": 1,
        "status": "ERROR",
        "message": "An error occurred",
        "data": {}
    }

    json_object = json.loads(json_string)

    # Acceder a los atributos del JSON
    openai_secret_key = json_object.get('openAiSecretKey')
    model = json_object.get('model')
    pathFileTrain = json_object.get('pathFileTrain')
    custom_http_client = httpx.Client(trust_env=False)

    # Configuración del cliente de OpenAI con la clave API
    client = OpenAI(
        api_key=openai_secret_key,
        http_client=custom_http_client
    )

    # Nombre del modelo preentrenado de OpenAI
    MODEL_ENGINE = model  # Ejemplo con GPT-3.5

    if not pathFileTrain:
        result["message"] = "No se recibió la URL del dataset de entrenamiento."
        return json.dumps(result)

    # OpenAI validates the uploaded file object's name, not only its contents.
    # A deterministic local name avoids query strings or download URLs hiding the extension.
    file_fingerprint = hashlib.sha256(pathFileTrain.encode("utf-8")).hexdigest()[:16]
    local_filename = str(Path("tmpFileTrain") / f"training-{file_fingerprint}.jsonl")
    print(f"Nombre del archivo local: {local_filename}")

    # Crear el directorio si no existe
    os.makedirs(os.path.dirname(local_filename), exist_ok=True)

    # Descargar el archivo desde la URL proporcionada
    response = requests.get(pathFileTrain, timeout=60)
    if response.status_code == 200:
        try:
            content = response.content.decode("utf-8-sig")
            valid_rows = 0
            for line_number, line in enumerate(content.splitlines(), start=1):
                if not line.strip():
                    continue
                try:
                    row = json.loads(line)
                except json.JSONDecodeError as json_error:
                    raise ValueError(
                        f"línea física {line_number}: {json_error.msg} en columna {json_error.colno}. "
                        "El dataset fue cortado por un salto de línea interno; elimínelo y créelo nuevamente."
                    ) from json_error
                messages = row.get("messages") if isinstance(row, dict) else None
                if not isinstance(messages, list) or not messages:
                    raise ValueError(f"la línea {line_number} no contiene messages")
                roles = {message.get("role") for message in messages if isinstance(message, dict)}
                if "user" not in roles or "assistant" not in roles:
                    raise ValueError(f"la línea {line_number} requiere mensajes user y assistant")
                valid_rows += 1
            if valid_rows == 0:
                raise ValueError("el dataset está vacío")
            with open(local_filename, "w", encoding="utf-8", newline="\n") as local_file:
                local_file.write(content.rstrip("\r\n") + "\n")
            print(f"Dataset JSONL validado: {valid_rows} filas")
        except (UnicodeDecodeError, json.JSONDecodeError, ValueError) as validation_error:
            result["message"] = f"El archivo descargado no es un JSONL de fine-tuning válido: {validation_error}"
            return json.dumps(result)
    else:
        result["message"] = f"Error al descargar el archivo: {response.status_code}"
        return json.dumps(result)

    # Subir el archivo descargado a OpenAI para el fine-tuning
    try:
        with open(local_filename, "rb") as training_file:
            if not training_file.name.lower().endswith(".jsonl"):
                raise ValueError("El archivo temporal debe terminar en .jsonl")
            uploaded_file = client.files.create(file=training_file, purpose="fine-tune")

        # Iniciar el proceso de fine-tuning con el archivo subido
        fine_tuning_details = client.fine_tuning.jobs.create(
            training_file=uploaded_file.id,
            model=MODEL_ENGINE
        )

        # Obtener los detalles del trabajo de fine-tuning
        fine_tuning_details = client.fine_tuning.jobs.retrieve(fine_tuning_details.id)

        # ftjob-oy92QjuL8JfypKkvDRSlOg0r
        #fine_tuning_details = client.fine_tuning.jobs.retrieve("ftjob-oy92QjuL8JfypKkvDRSlOg0r");

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
        result["data"] = fine_tuning_response_dict

        print(f"Fine-tuning iniciado: {fine_tuning_response_dict}")
    except Exception as e:
        result["message"] = f"Error al subir el archivo o iniciar el fine-tuning: {e}"
        print(result["message"])

    return json.dumps(result)


def _openai_object_to_dict(value):
    if value is None:
        return None
    if hasattr(value, "model_dump"):
        return value.model_dump(mode="json")
    if hasattr(value, "to_dict"):
        return value.to_dict()
    return value


def get_training_status(json_string):
    request = json.loads(json_string)
    result = {"code": 1, "status": "ERROR", "message": "Unable to monitor training", "data": {}}
    api_key = request.get("openAiSecretKey")
    job_id = request.get("jobId")
    if not api_key or not job_id:
        result["message"] = "openAiSecretKey and jobId are required"
        return json.dumps(result)
    try:
        client = OpenAI(api_key=api_key, http_client=httpx.Client(trust_env=False))
        job = client.fine_tuning.jobs.retrieve(job_id)
        events_page = client.fine_tuning.jobs.list_events(fine_tuning_job_id=job_id, limit=100)
        checkpoints_page = client.fine_tuning.jobs.checkpoints.list(fine_tuning_job_id=job_id, limit=100)
        events = [_openai_object_to_dict(item) for item in events_page.data]
        checkpoints = [_openai_object_to_dict(item) for item in checkpoints_page.data]

        progress_percent = None
        for event in events:
            data = event.get("data") or {}
            step = data.get("step") or data.get("step_number")
            total_steps = data.get("total_steps")
            if step is not None and total_steps:
                progress_percent = min(99, round(float(step) * 100 / float(total_steps), 2))
                break
        if job.status == "succeeded":
            progress_percent = 100

        result.update({
            "code": 0,
            "status": "OK",
            "message": "Training status retrieved",
            "data": {
                "job": _openai_object_to_dict(job),
                "events": events,
                "checkpoints": checkpoints,
                "progress_percent": progress_percent,
                "checked_at": datetime.now(timezone.utc).isoformat(),
            },
        })
    except Exception as error:
        result["message"] = f"Error al consultar el entrenamiento {job_id}: {error}"
    return json.dumps(result)

def use_model (json_string):
    started = time.monotonic()
    print(f"Inicia proceso para usar el modelo.");

    result = {
        "code": 1,
        "status": "ERROR",
        "message": "An error occurred",
        "data": {}
    }

    json_object = json.loads(json_string)

    # Acceder a los atributos del JSON
    openai_secret_key = json_object.get('openAiSecretKey')
    model = json_object.get('model')
    messages = json_object.get('messages');
    response_schema = json_object.get('responseSchema')
    max_output_tokens = json_object.get('maxOutputTokens') or 4000
    custom_http_client = httpx.Client(trust_env=False, timeout=float(INFERENCE_TIMEOUT_SECONDS))

    input_characters = sum(len(str(message.get("content", ""))) for message in (messages or []))
    result["diagnostics"] = {"requested_model": model, "input_characters": input_characters,
                             "timeout_seconds": INFERENCE_TIMEOUT_SECONDS, "max_output_tokens": max_output_tokens,
                             "reasoning_effort": json_object.get("reasoningEffort") or "provider_default"}
    print(f"Solicitud recibida: modelo={model!r}; caracteres={input_characters}; "
          f"salida_maxima={max_output_tokens}; timeout={INFERENCE_TIMEOUT_SECONDS}s; "
          f"razonamiento={result['diagnostics']['reasoning_effort']}; esquema={response_schema or 'json_object'}", flush=True)

    # Configuración del cliente de OpenAI con la clave API
    try:
        client = OpenAI(
            api_key=openai_secret_key,
            http_client=custom_http_client,
            timeout=float(INFERENCE_TIMEOUT_SECONDS),
            max_retries=0
        )

        # Nombre del modelo preentrenado de OpenAI
        MODEL_ENGINE = model  # Ejemplo con GPT-3.5

        # Iniciar el proceso de consulta al modelo
        response_format = (CASE_WORKSPACE_RESPONSE_FORMAT
                           if response_schema == "case_workspace"
                           else {"type": "json_object"})
        parameters = {"model": MODEL_ENGINE, "response_format": response_format,
                      "max_completion_tokens": max_output_tokens}
        temperature = json_object.get("temperature", 0.2)
        if temperature is not None:
            parameters["temperature"] = temperature
        if json_object.get("reasoningEffort"):
            parameters["reasoning_effort"] = json_object["reasoningEffort"]
        result["request_parameters"] = parameters
        openai_response = client.chat.completions.create(messages=messages, **parameters)

        # Procesar la respuesta para asegurar que sea JSON
        json_response = openai_response.choices[0].message.content

        # Imprimir los detalles para verificar
        print(f"Respuesta del modelo: {json_response}")

        result["code"] = 0
        result["status"] = "OK"
        result["message"] = "Transaction successfully completed"
        result["data"] = json_response
        result["finish_reason"] = openai_response.choices[0].finish_reason
        result["refusal"] = openai_response.choices[0].message.refusal
        if openai_response.usage:
            result["usage"] = _openai_object_to_dict(openai_response.usage)
        result["model"] = openai_response.model
        if getattr(openai_response, "service_tier", None):
            result["service_tier"] = openai_response.service_tier
        result["response_schema"] = response_schema
        result["diagnostics"]["request_id"] = getattr(openai_response, "_request_id", None)
        result["diagnostics"]["finish_reason"] = result["finish_reason"]
        result["diagnostics"]["output_characters"] = len(json_response or "")
        if result.get("usage"):
            result["diagnostics"]["completion_tokens"] = result["usage"].get("completion_tokens")
            result["diagnostics"]["reasoning_tokens"] = (result["usage"].get("completion_tokens_details") or {}).get("reasoning_tokens")

        print(f"Finaliza proceso para usar el modelo.");

    except Exception as e:
        detail = str(e).replace(openai_secret_key, "[REDACTED]") if openai_secret_key else str(e)
        result["diagnostics"]["error_type"] = type(e).__name__
        result["diagnostics"]["cause_type"] = type(e.__cause__).__name__ if e.__cause__ else None
        result["diagnostics"]["request_id"] = getattr(e, "request_id", None)
        if isinstance(e, APITimeoutError):
            result["message"] = (f"OpenAI no devolvió la respuesta dentro del tiempo de espera HTTP de {INFERENCE_TIMEOUT_SECONDS} segundos "
                                 f"(modelo: {model}; causa: {result['diagnostics']['cause_type'] or 'timeout'}). "
                                 "La solicitud no se reintentó automáticamente. Revise los diagnósticos del servicio Python.")
        else:
            result["message"] = f"Error al crear el chat: {detail}"
        print(result["message"], flush=True)
    finally:
        result["diagnostics"]["elapsed_ms"] = round((time.monotonic() - started) * 1000)
        print(f"Diagnóstico de generación: {json.dumps(result['diagnostics'], ensure_ascii=False)}", flush=True)
        custom_http_client.close()

    return json.dumps(result)
