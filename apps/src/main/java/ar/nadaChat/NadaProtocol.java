package ar.nadaChat;

import ar.thorium.handler.EventHandlerFactory;
import ar.thorium.utils.ChannelFacade;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: facundo
 * Date: 02/11/13
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
public class NadaProtocol implements EventHandlerFactory<NadaHandler> {
    Map<ChannelFacade, NadaUser> users =
            Collections.synchronizedMap (new HashMap<ChannelFacade, NadaUser>());

    // --------------------------------------------------
    // Implementation of InputHandlerFactory interface

    public NadaHandler newHandler() throws IllegalAccessException, InstantiationException
    {
        return new NadaHandler(this);
    }

    // --------------------------------------------------

    void newUser (ChannelFacade facade)
    {
        NadaUser user = new NadaUser (facade);

        users.put (facade, user);
        user.send(user.getNickName() + "\n");
    }

    void endUser (ChannelFacade facade)
    {
        users.remove (facade);
    }

    public void handleMessage (ChannelFacade facade, NadaMessage message)
    {
        broadcast(users.get (facade), message.getMessage());
    }

    private void broadcast (NadaUser sender, String message)
    {
        synchronized (users) {
            for (NadaUser user : users.values()) {
                if (user != sender) {
                    sender.sendTo (user, message);
                }
            }
        }
    }

    // ----------------------------------------------------

    private static class NadaUser
    {
        private final ChannelFacade facade;
        private String nickName;
        private String prefix = null;
        private static int counter = 1;

        public NadaUser (ChannelFacade facade)
        {
            this.facade = facade;
            setNickName ("nick-" + counter++);
        }

        public void send (String message)
        {
            facade.outputQueue().enqueue(ByteBuffer.wrap(message.getBytes()));
        }

        public void sendTo (NadaUser recipient, String message)
        {
            recipient.send(prefix);
            recipient.send(message);
        }

        public String getNickName ()
        {
            return nickName;
        }

        public void setNickName (String nickName)
        {
            this.nickName = nickName;

            String prefixStr = "[" + nickName + "] ";

            prefix = prefixStr;
        }
    }
}
