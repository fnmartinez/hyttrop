package ar.edu.itba.pdc.commands;

import ar.edu.itba.pdc.administration.AdminProtocol;
import ar.edu.itba.pdc.transformations.L33tTransformation;
import ar.edu.itba.pdc.transformations.TransformationChain;
import ar.edu.itba.pdc.utils.ByteArrayQueue;

public class SetL33tCommand implements Command {

    @Override
    public String getName() {
        return "l33t";
    }

    @Override
    public String execute(String[] args) {

        AdminProtocol.AdminProtocolActions action = AdminProtocol.AdminProtocolActions.getAction(args[0]);

        switch (action){
            case SET:
                if(args.length > 2){

                    L33tTransformation t = new L33tTransformation();
                    TransformationChain transformations = TransformationChain.getInstance();

                    if(args[2].equals("on")){
                    	transformations.addL33t();
                        return "Transformacion l33t encendida.\n";
                    }
                    else if(args[2].equals("off")){
                    	transformations.removeL33t();
                        return "Transformacion l33t apagada.\n";
                    }else{
                        return this.shortHelp();
                    }
                }else{
                    return this.shortHelp();
                }
            case HELP:
                return this.descriptiveHelp();
            default:
                return "Unsupported message.\n";
        }
    }

    @Override
    public String shortHelp() {
        return "[set|help] l33t [on|off]";
    }

    @Override
    public String descriptiveHelp() {
        return "Con este comando se podra activar la transformacion l33t en los mensajes de tipo text/plain.\n[set|help] l33t [on|off]\n";
    }

    @Override
    public boolean acceptsAction(AdminProtocol.AdminProtocolActions action) {
        if(action == AdminProtocol.AdminProtocolActions.GET){
            return false;
        }
        return true;
    }
}
