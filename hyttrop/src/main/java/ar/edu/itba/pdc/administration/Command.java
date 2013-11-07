package ar.edu.itba.pdc.administration;

import ar.edu.itba.pdc.administration.AdminProtocol.AdminProtocolActions;
import ar.thorium.utils.Message;

public interface Command extends Message{

    String getName();

    String execute(String[] args);

    String shortHelp();

    String descriptiveHelp();

    boolean acceptsAction(AdminProtocolActions action);
}

