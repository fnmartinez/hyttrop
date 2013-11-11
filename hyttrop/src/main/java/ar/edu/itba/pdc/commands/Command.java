package ar.edu.itba.pdc.commands;

import ar.edu.itba.pdc.administration.AdminProtocol.AdminProtocolActions;

public interface Command{

    String getName();

    String execute(String[] args);

    String shortHelp();

    String descriptiveHelp();

    boolean acceptsAction(AdminProtocolActions action);
}

