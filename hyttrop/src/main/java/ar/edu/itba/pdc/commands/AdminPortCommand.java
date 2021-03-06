package ar.edu.itba.pdc.commands;

import ar.edu.itba.pdc.Hyttrop;
import ar.edu.itba.pdc.administration.AdminProtocol;
import ar.thorium.acceptor.implementations.BasicSocketAcceptor;

import java.net.InetSocketAddress;

public class AdminPortCommand implements Command {
    @Override
    public String getName() {
        return "admin-port";
    }

    @Override
    public String execute(String[] args) {
        AdminProtocol.AdminProtocolActions action = AdminProtocol.AdminProtocolActions.getAction(args[0]);
        if (action == null) {
            return "Unknown Command";
        }
        switch (action) {
            case GET:
                return "\r\nProxy port: " + ((BasicSocketAcceptor) Hyttrop.ADMINISTRATION.getAcceptor()).getBindingAddress().getPort();
            case SET:
                try{
                    Hyttrop.ADMINISTRATION.getAcceptor().stop();
                    InetSocketAddress oldAddress = ((BasicSocketAcceptor)Hyttrop.ADMINISTRATION.getAcceptor()).getBindingAddress();
                    InetSocketAddress newAddress = new InetSocketAddress(oldAddress.getHostName(), Integer.parseInt(args[2]));
                    if (!oldAddress.equals(newAddress)) {
                        ((BasicSocketAcceptor)Hyttrop.ADMINISTRATION.getAcceptor()).setBindingAddress(newAddress);
                        Hyttrop.ADMINISTRATION.getAcceptor().start();
                    }
                    return "Port successfully changed\r\nOld port: " +
                            oldAddress.getPort() + "\r\nNew port: " + newAddress.getPort();
                } catch (Exception e) {
                    return "Unnable to change port. You might need to restart proxy";
                }
            case HELP:
                return this.descriptiveHelp();
            default:
                return "Unsupported Message";
        }
    }

    @Override
    public String shortHelp() {
        return "[set|get] admin-port <port>";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String descriptiveHelp() {
        return "Sets or gets the administration port";  //To change body of implemented methods use File | Settings | File Templates.
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
