from concurrent import futures
import grpc
import json
from grpc_service.proto import openai_pb2_grpc, openai_pb2
from openai_model.train_model import train_model
from openai_model.train_model import use_model


class OpenAIServiceServicer(openai_pb2_grpc.OpenAIServiceServicer):
    def TrainModel(self, request, context):
        try:
            print(request);
            # Procesar la cadena JSON recibida
            data = request.json_data;
            # Lógica de procesamiento del modelo usando los datos JSON
            response_message = train_model(data)
            print(response_message);
            # Crear y devolver la respuesta
            response = openai_pb2.TrainResponse()
            response.message = f"Processing result: {response_message}"
            return response
        except json.JSONDecodeError as e:
            context.set_details(f"Invalid JSON: {str(e)}")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            return openai_pb2.TrainResponse()
        except Exception as e:
            context.set_details(f"Error processing request: {str(e)}")
            context.set_code(grpc.StatusCode.UNKNOWN)
            return openai_pb2.TrainResponse()

    def UseModel(self, request, context):
        try:
            print(request);
            # Procesar la cadena JSON recibida
            data = request.json_data;
            # Lógica de procesamiento del modelo usando los datos JSON
            response_message = use_model(data)
            print(response_message);
            # Crear y devolver la respuesta
            response = openai_pb2.TrainResponse()
            response.message = f"Processing result: {response_message}"
            return response
        except json.JSONDecodeError as e:
            context.set_details(f"Invalid JSON: {str(e)}")
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            return openai_pb2.TrainResponse()
        except Exception as e:
            context.set_details(f"Error processing request: {str(e)}")
            context.set_code(grpc.StatusCode.UNKNOWN)
            return openai_pb2.TrainResponse()


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    openai_pb2_grpc.add_OpenAIServiceServicer_to_server(OpenAIServiceServicer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
