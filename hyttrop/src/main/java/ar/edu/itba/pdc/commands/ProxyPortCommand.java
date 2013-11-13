package ar.edu.itba.pdc.commands;

import ar.edu.itba.pdc.Hyttrop;
import ar.edu.itba.pdc.administration.AdminProtocol;
import ar.thorium.acceptor.implementations.BasicSocketAcceptor;

import java.net.InetSocketAddress;

public class ProxyPortCommand implements Command{
    @Override
    public String getName() {
        return "proxy-port";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String execute(String[] args) {
        AdminProtocol.AdminProtocolActions action = AdminProtocol.AdminProtocolActions.getAction(args[0]);
        if (action == null) {
            return AdminProtocol.createErrorResponse("Unknown Command");
        }
        switch (action) {
            case GET:
                return AdminProtocol.createSuccessResponse("\r\nProxy port: " + ((BasicSocketAcceptor)Hyttrop.HYTTROP.getAcceptor()).getBindingAddress().getPort());
            case SET:
                try{
                    Hyttrop.HYTTROP.getAcceptor().stop();
                    InetSocketAddress oldAddress = ((BasicSocketAcceptor)Hyttrop.HYTTROP.getAcceptor()).getBindingAddress();
                    InetSocketAddress newAddress = new InetSocketAddress(oldAddress.getHostName(), Integer.parseInt(args[2]));
                    if (!oldAddress.equals(newAddress)) {
                        ((BasicSocketAcceptor)Hyttrop.HYTTROP.getAcceptor()).setBindingAddress(newAddress);
                        Hyttrop.HYTTROP.getAcceptor().start();
                    }
                    return AdminProtocol.createSuccessResponse("Port successfully changed\r\nOld port: " +
                            oldAddress.getPort() + "\r\nNew port: " + newAddress.getPort());
                } catch (Exception e) {
                    return AdminProtocol.createErrorResponse("Unnable to change port. You might need to restart proxy");
                }
            case HELP:
                return AdminProtocol.createSuccessResponse(this.descriptiveHelp());
            default:
                return AdminProtocol.createErrorResponse("Unsupported Message");
        }
    }

    @Override
    public String shortHelp() {
        return "[set|get|help] proxy-port port.";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String descriptiveHelp() {
        return "Sets or gets the proxy port.";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean acceptsAction(AdminProtocol.AdminProtocolActions action) {
        switch (action) {
            case GET:
            case SET:
            case HELP:
                return true;
        }
        return false;
    }
}
