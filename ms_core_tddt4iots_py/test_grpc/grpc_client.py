import grpc
from grpc_service.proto import openai_pb2_grpc, openai_pb2

def run():
    with grpc.insecure_channel('localhost:50051') as channel:
        stub = openai_pb2_grpc.OpenAIServiceStub(channel)
        try:
            # Enviar un JSON válido en formato de string
            json_data = '{"key": "value"}'
            response = stub.TrainModel(openai_pb2.TrainRequest(json_data=json_data))
            print("Client received: " + response.message)
        except grpc.RpcError as e:
            print(f"RPC failed: {e}")

if __name__ == '__main__':
    run()
