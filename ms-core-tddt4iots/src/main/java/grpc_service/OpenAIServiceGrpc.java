package grpc_service;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.57.0)",
    comments = "Source: openai.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class OpenAIServiceGrpc {

  private OpenAIServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "grpc_service.OpenAIService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<grpc_service.Openai.TrainRequest,
      grpc_service.Openai.TrainResponse> getTrainModelMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TrainModel",
      requestType = grpc_service.Openai.TrainRequest.class,
      responseType = grpc_service.Openai.TrainResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc_service.Openai.TrainRequest,
      grpc_service.Openai.TrainResponse> getTrainModelMethod() {
    io.grpc.MethodDescriptor<grpc_service.Openai.TrainRequest, grpc_service.Openai.TrainResponse> getTrainModelMethod;
    if ((getTrainModelMethod = OpenAIServiceGrpc.getTrainModelMethod) == null) {
      synchronized (OpenAIServiceGrpc.class) {
        if ((getTrainModelMethod = OpenAIServiceGrpc.getTrainModelMethod) == null) {
          OpenAIServiceGrpc.getTrainModelMethod = getTrainModelMethod =
              io.grpc.MethodDescriptor.<grpc_service.Openai.TrainRequest, grpc_service.Openai.TrainResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TrainModel"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc_service.Openai.TrainRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc_service.Openai.TrainResponse.getDefaultInstance()))
              .setSchemaDescriptor(new OpenAIServiceMethodDescriptorSupplier("TrainModel"))
              .build();
        }
      }
    }
    return getTrainModelMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OpenAIServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OpenAIServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OpenAIServiceStub>() {
        @java.lang.Override
        public OpenAIServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OpenAIServiceStub(channel, callOptions);
        }
      };
    return OpenAIServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OpenAIServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OpenAIServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OpenAIServiceBlockingStub>() {
        @java.lang.Override
        public OpenAIServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OpenAIServiceBlockingStub(channel, callOptions);
        }
      };
    return OpenAIServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OpenAIServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OpenAIServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OpenAIServiceFutureStub>() {
        @java.lang.Override
        public OpenAIServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OpenAIServiceFutureStub(channel, callOptions);
        }
      };
    return OpenAIServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void trainModel(grpc_service.Openai.TrainRequest request,
        io.grpc.stub.StreamObserver<grpc_service.Openai.TrainResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTrainModelMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service OpenAIService.
   */
  public static abstract class OpenAIServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return OpenAIServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service OpenAIService.
   */
  public static final class OpenAIServiceStub
      extends io.grpc.stub.AbstractAsyncStub<OpenAIServiceStub> {
    private OpenAIServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenAIServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OpenAIServiceStub(channel, callOptions);
    }

    /**
     */
    public void trainModel(grpc_service.Openai.TrainRequest request,
        io.grpc.stub.StreamObserver<grpc_service.Openai.TrainResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTrainModelMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service OpenAIService.
   */
  public static final class OpenAIServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<OpenAIServiceBlockingStub> {
    private OpenAIServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenAIServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OpenAIServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public grpc_service.Openai.TrainResponse trainModel(grpc_service.Openai.TrainRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTrainModelMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service OpenAIService.
   */
  public static final class OpenAIServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<OpenAIServiceFutureStub> {
    private OpenAIServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenAIServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OpenAIServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc_service.Openai.TrainResponse> trainModel(
        grpc_service.Openai.TrainRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTrainModelMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_TRAIN_MODEL = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_TRAIN_MODEL:
          serviceImpl.trainModel((grpc_service.Openai.TrainRequest) request,
              (io.grpc.stub.StreamObserver<grpc_service.Openai.TrainResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getTrainModelMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc_service.Openai.TrainRequest,
              grpc_service.Openai.TrainResponse>(
                service, METHODID_TRAIN_MODEL)))
        .build();
  }

  private static abstract class OpenAIServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OpenAIServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return grpc_service.Openai.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OpenAIService");
    }
  }

  private static final class OpenAIServiceFileDescriptorSupplier
      extends OpenAIServiceBaseDescriptorSupplier {
    OpenAIServiceFileDescriptorSupplier() {}
  }

  private static final class OpenAIServiceMethodDescriptorSupplier
      extends OpenAIServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    OpenAIServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (OpenAIServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OpenAIServiceFileDescriptorSupplier())
              .addMethod(getTrainModelMethod())
              .build();
        }
      }
    }
    return result;
  }
}
