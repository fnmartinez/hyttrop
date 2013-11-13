package ar.edu.itba.pdc.commands;

import ar.edu.itba.pdc.administration.AdminProtocol;
import ar.edu.itba.pdc.transformations.L33tTransformation;
import ar.edu.itba.pdc.transformations.TransformationChain;

public class SetL33tCommand implements Command {

    @Override
    public String getName() {
        return "l33t";
    }

    @Override
    public String execute(String[] args) {

        if(args.length > 2){

            L33tTransformation t = L33tTransformation.getInstance();
            TransformationChain transformations = TransformationChain.getInstance();

            if(args[2].equals("on")){
                transformations.add(t);
                return "Transformacion l33t encendida.\n";
            }
            else if(args[2].equals("off")){
                transformations.remove(t);
                return "Transformacion l33t apagada.\n";
            }else{
                return this.shortHelp();
            }
        }else{
            return this.shortHelp();
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
