package ar.edu.itba.pdc.administration;

import ar.edu.itba.pdc.commands.Command;

import org.apache.commons.lang.StringUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

public class AdminProtocol {

    public static final String EOL = "\r\n";
    public static final String END_OF_RESPONSE = EOL.concat(".").concat(EOL);
    public static final String OK_RESPONSE = "+OK";
    public static final String ERR_RESPONSE = "-ERR";
    private static final Map<String, Command> commands = new HashMap<>();

    public enum AdminProtocolActions {
        SET("set"), GET("get"), HELP("help");

        public static Map<String, AdminProtocolActions> actionsMap;

        static {
            actionsMap = new HashMap<>();
            actionsMap.put(SET.string, SET);
            actionsMap.put(GET.string, GET);
            actionsMap.put(HELP.string, HELP);
        }
        public final String string;

        private AdminProtocolActions(String string) {
            this.string = string;
        }

        public static AdminProtocolActions getAction(String name) {
            return actionsMap.get(name);
        }
    }

    private Charset charset = Charset.forName("UTF-8");
    private CharsetDecoder decoder;
    private CharsetEncoder encoder;

    public AdminProtocol() {
        this.charset = Charset.forName("UTF-8");
        this.decoder = charset.newDecoder();
        this.encoder = charset.newEncoder();
    }

    public ByteBuffer handleMessage(AdminMessage message) {
        String commandLine = message.getMessage();
        String response = interpretCommand(commandLine);
        ByteBuffer encodedResponse = encodeMessage(response);
        return encodedResponse;
    }

    private String interpretCommand(String commandLine) {
        String[] tokens = commandLine.split("\\s");
        Command cmd;
        if (tokens.length == 1)
        {
            if (tokens[0].equalsIgnoreCase(AdminProtocolActions.HELP.string)) {
                StringBuilder sb = new StringBuilder();
                for(Command c : commands.values()) {
                    sb.append(EOL).append(c.shortHelp());
                }
                return createSuccessResponse(sb.toString());
            }
            else{
                return createErrorResponse("Action unknown or unsupported.\n");
            }
        }
        if ((cmd = commands.get(tokens[1])) != null) {
            if (tokens[0].equalsIgnoreCase(AdminProtocolActions.GET.string)) {
                if (cmd.acceptsAction(AdminProtocolActions.GET)) {
                    return createSuccessResponse(cmd.execute(tokens));
                } else {
                    return createErrorResponse("This command does not support GET method.\n");
                }
            }
            if (tokens[0].equalsIgnoreCase(AdminProtocolActions.SET.string)) {
                if (cmd.acceptsAction(AdminProtocolActions.SET)) {
                    return createSuccessResponse(cmd.execute(tokens));
                } else {
                    return createErrorResponse("This command does not support SET method.\n");
                }
            }
            if (tokens[0].equalsIgnoreCase(AdminProtocolActions.HELP.string)) {
                if (cmd.acceptsAction(AdminProtocolActions.HELP)) {
                    return cmd.descriptiveHelp();
                } else {
                    return createErrorResponse("This command does not support HELP method.\n");
                }
            }
            return createErrorResponse("Action unknown or unsupported.\n");
        } else {
            return createErrorResponse("Unknown command.\n");
        }
    }

    public static String createSuccessResponse(String response) {
        return new StringBuilder().append(OK_RESPONSE).append(" ").append(response).append(END_OF_RESPONSE).toString();
    }

    public static String createErrorResponse(String response) {
        return new StringBuilder().append(ERR_RESPONSE).append(" ").append(response).append(END_OF_RESPONSE).toString();
    }

    public void addCommand(Command c) throws UncompliantAdministrativeCommandException {
        if (!c.acceptsAction(AdminProtocolActions.HELP)
                || StringUtils.isBlank(c.descriptiveHelp())
                || StringUtils.isBlank(c.shortHelp())
                || StringUtils.isBlank(c.getName())) {
            throw new UncompliantAdministrativeCommandException();
        }
        commands.put(c.getName(), c);
    }

    public void deleteCommand(Command c) {
        commands.remove(c.getName());
    }

    @SuppressWarnings("unused")
	private String decodeMessage(ByteBuffer message) {
        String data = "";
        int old_position = message.position();
        try {
            data = decoder.decode(message).toString();
        } catch (CharacterCodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

        message.position(old_position);
        return data;
    }

    private ByteBuffer encodeMessage(String message) {
        try {
            return encoder.encode(CharBuffer.wrap(message));
        } catch (CharacterCodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ByteBuffer.allocate(0).asReadOnlyBuffer();
    }

    public ByteBuffer receptionMessage() {
        String response = OK_RESPONSE.concat(END_OF_RESPONSE);
        ByteBuffer bf = ByteBuffer.allocate(response.length());
        bf.put(response.getBytes());
        return bf;
    }
}
