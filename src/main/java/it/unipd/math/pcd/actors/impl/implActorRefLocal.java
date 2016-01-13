package it.unipd.math.pcd.actors.impl;

import it.unipd.math.pcd.actors.Actor;
import it.unipd.math.pcd.actors.ActorRef;
import it.unipd.math.pcd.actors.Message;

/**
 * Defines common properties of all actors.
 *
 * @author Davide Rigoni
 * @version 1.0
 * @since 1.0
 */
public class implActorRefLocal implements ActorRef{

    @Override
    public void send(Message message, ActorRef to) {
        implActorSystem as = new implActorSystem();
        Actor a = as.getUnderlyingActor(to);
        a.addMessage(message,this);
    }

    @Override
    public int compareTo(Object o) {
        if(this.equals(o))
            return 0;
        else
            return 1;
    }
}
