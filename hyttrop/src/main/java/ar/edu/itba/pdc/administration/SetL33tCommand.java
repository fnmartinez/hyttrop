package ar.edu.itba.pdc.administration;

import ar.edu.itba.pdc.transformations.L33tTransformation;
import ar.edu.itba.pdc.transformations.TransformationChain;

public class SetL33tCommand implements Command {


    @Override
    public String getName() {
        return "l33t";
    }

    @Override
    public String execute(String[] args) {

        L33tTransformation t = L33tTransformation.getInstance();
        TransformationChain transformations = TransformationChain.getInstance();
        transformations.add(t);

        return "Comando ejecutado correctamente.\n";
    }

    @Override
    public String shortHelp() {
        return "[set|help] l33t [<USERNAME>|all]\n";
    }

    @Override
    public String descriptiveHelp() {
        return "Con este comando se podra activar la transformacion l33t en los usuarios.\n[set|help] l33t [<USERNAME>|all]\n";
    }

    @Override
    public boolean acceptsAction(AdminProtocol.AdminProtocolActions action) {
        // TODO Auto-generated method stub
        return true;
    }
}
