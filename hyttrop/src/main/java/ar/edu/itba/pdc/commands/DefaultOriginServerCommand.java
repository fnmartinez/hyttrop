package ar.edu.itba.pdc.commands;

import ar.edu.itba.pdc.administration.AdminProtocol;
import ar.edu.itba.pdc.utils.ConfigurationHelper;

import java.net.InetSocketAddress;

public class DefaultOriginServerCommand implements Command{
    @Override
    public String getName() {
        return "default-origin-server";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String execute(String[] args) {
        AdminProtocol.AdminProtocolActions action = AdminProtocol.AdminProtocolActions.getAction(args[0]);
        if (action == null) {
            return "Unknown Command";
        }
        switch (action) {
            case GET:
                InetSocketAddress origin = ConfigurationHelper.getInstance().getDefaultOriginServerAddress();
                return origin == null? "No origin server set" : origin.toString();
            case SET:
                try {
                    InetSocketAddress oldOrigin = ConfigurationHelper.getInstance().getDefaultOriginServerAddress();
                    InetSocketAddress newOrigin = new InetSocketAddress(args[2], Integer.parseInt(args[3]));
                    if (!newOrigin.equals(oldOrigin)) {
                        ConfigurationHelper.getInstance().setDefaultOriginServerAddress(newOrigin);
                    }
                    return "Default origin server changed successfully";
                } catch (Exception e) {
                    return "Could not change default origin server";
                }
            case HELP:
                return this.descriptiveHelp();
            default:
                return "Unsupported Message";
        }
    }

    @Override
    public String shortHelp() {
        return "[set|get] default-origin-server <hostname> <port>";
    }

    @Override
    public String descriptiveHelp() {
        return "Sets or gets the default origin server for every connection. Useful for chaining proxys";
    }

    @Override
    public boolean acceptsAction(AdminProtocol.AdminProtocolActions action) {
        switch (action) {
            case SET:
            case GET:
            case HELP:
                return true;
        }
        return false;
    }
}
