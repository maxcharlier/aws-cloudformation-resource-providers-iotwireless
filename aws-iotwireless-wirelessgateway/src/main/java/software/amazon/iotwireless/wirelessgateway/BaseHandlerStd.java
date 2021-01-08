package software.amazon.iotwireless.wirelessgateway;

import software.amazon.awssdk.services.iotwireless.IotWirelessClient;
import software.amazon.awssdk.services.iotwireless.model.IotWirelessRequest;
import software.amazon.awssdk.services.iotwireless.model.DeleteWirelessGatewayRequest;
import software.amazon.awssdk.services.iotwireless.model.GetWirelessGatewayRequest;
import software.amazon.awssdk.services.iotwireless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.iotwireless.model.UpdateWirelessGatewayRequest;
import software.amazon.awssdk.services.iotwireless.model.AccessDeniedException;
import software.amazon.awssdk.services.iotwireless.model.ConflictException;
import software.amazon.awssdk.services.iotwireless.model.InternalServerException;
import software.amazon.awssdk.services.iotwireless.model.ThrottlingException;
import software.amazon.awssdk.services.iotwireless.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProgressEvent;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    public enum IdType
    {
        WirelessGatewayId, GatewayEui;
    }
    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<IotWirelessClient> proxyClient,
            final Logger logger);

    private String extractResourceIdFromRequests(final Object request) {
        if (request instanceof DeleteWirelessGatewayRequest) {
            final DeleteWirelessGatewayRequest deleteWirelessGatewayRequest = (DeleteWirelessGatewayRequest) request;
            return deleteWirelessGatewayRequest.id();
        } if (request instanceof GetWirelessGatewayRequest) {
            final GetWirelessGatewayRequest getWirelessGatewayRequest = (GetWirelessGatewayRequest) request;
            return getWirelessGatewayRequest.identifier();
        } if (request instanceof UpdateWirelessGatewayRequest) {
            final UpdateWirelessGatewayRequest updateWirelessGatewayRequest = (UpdateWirelessGatewayRequest) request;
            return updateWirelessGatewayRequest.id();
        } else {
            return "";
        }
    }

    public RuntimeException handleException(final Exception error, final IotWirelessRequest request) {
        if (error instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(ResourceModel.TYPE_NAME, extractResourceIdFromRequests(request));
        } if (error instanceof ConflictException) {
            return new CfnResourceConflictException(error);
        } if (error instanceof InternalServerException) {
            return new CfnInternalFailureException(error);
        } if (error instanceof ThrottlingException) {
            return new CfnThrottlingException(ResourceModel.TYPE_NAME, error);
        } if (error instanceof ValidationException) {
            return new CfnInvalidRequestException(ResourceModel.TYPE_NAME, error);
        } if (error instanceof AccessDeniedException) {
            return new CfnAccessDeniedException(ResourceModel.TYPE_NAME, error);
        } else {
            return new CfnGeneralServiceException(ResourceModel.TYPE_NAME, error);
        }
    }
}