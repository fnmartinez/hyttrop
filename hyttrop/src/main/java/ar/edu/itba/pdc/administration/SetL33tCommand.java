package ar.edu.itba.pdc.administration;

import java.util.ArrayList;

import java.util.ArrayList;

import ar.edu.itba.it.pdc.jabxy.model.JabxyUser;
import ar.edu.itba.it.pdc.jabxy.model.administration.AdminProtocol.AdminProtocolActions;
import ar.edu.itba.it.pdc.jabxy.model.transformations.TransformationChain;
import ar.edu.itba.it.pdc.jabxy.model.transformations.TransformationL33t;
import ar.edu.itba.it.pdc.jabxy.services.UserService;

public class SetL33tCommand implements Command {

    @Override
    public String getName() {
        return "l33t";
    }

    @Override
    public String execute(String[] args) {


        TransformationL33t t = new TransformationL33t();



        ArrayList<JabxyUser> users = new ArrayList<JabxyUser>();

        if(args.length == 1 && args[0].equalsIgnoreCase("all")){
            users = UserService.getAllUsers();
        }else{
            for(String user : args){
                JabxyUser u = UserService.getUser(user);
                if(u == null){
                    return "ERROR: usuario '"+user+"' inexistente.";
                }
                users.add(u);
            }
        }

        for(JabxyUser user : users){
            TransformationChain transformations = user.getTransformations();
            transformations.add(t);
        }

        return "Comando ejecutado correctamente.";
    }

    @Override
    public String shortHelp() {
        return "[set|help] l33t [<USERNAME>|all]";
    }

    @Override
    public String descriptiveHelp() {
        return "Con este comando se podra activar la transformacion l33t en los usuarios.\n[set|help] l33t [<USERNAME>|all]";
    }

    @Override
    public boolean acceptsAction(AdminProtocolActions action) {
        // TODO Auto-generated method stub
        return false;
    }

}
