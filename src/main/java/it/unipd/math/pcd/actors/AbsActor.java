/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 Riccardo Cardin
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p/>
 * Please, insert description here.
 *
 * @author Riccardo Cardin
 * @author Davide Rigoni
 * @version 1.0
 * @since 1.0
 */
package it.unipd.math.pcd.actors;

import it.unipd.math.pcd.actors.exceptions.NoSuchActorException;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Defines common properties of all actors.
 *
 * @author Riccardo Cardin
 * @author Davide Rigoni
 * @version 1.0
 * @since 1.0
 */
public abstract class AbsActor<T extends Message> implements Actor<T> {

    /**
     * Self-reference of the actor
     */
    protected ActorRef<T> self;

    /**
     * Sender of the current message
     */
    protected ActorRef<T> sender;

    /**
     * Sets the self-referece.
     *
     * @param self The reference to itself
     * @return The actor.
     */
    protected final Actor<T> setSelf(ActorRef<T> self) {
        this.self = self;
        return this;
    }


    //---------------- ADDED METHODS -----------------

    /**
     * LinkedList to keep the message{@code messages} and the corresponding sender {@code senders}
     */
    private final Queue<T> messages = new ConcurrentLinkedQueue<>();
    private final Queue<ActorRef<T>> senders = new ConcurrentLinkedQueue<>();

    /**
     * Thread have to take messages from queue and do the jobs
     */
    private MessagesManager threadMM;

    /**
     * Actor state. When it is stopped sWorking is false;
     */
    private boolean sWorking;


    public void addMessage(T _m, ActorRef<T> _ar){
        synchronized (messages)
        {
            if(!sWorking)throw  new NoSuchActorException();
            messages.add(_m);
            senders.add(_ar);
            messages.notifyAll();
        }
    }


    public T removeMessage() throws InterruptedException {
        T m;
        synchronized (messages)
        {
            while(messages.size() == 0 )
            {
                messages.wait();
            }
            m = messages.poll();
            sender = senders.poll();
            messages.notifyAll();
        }
        return m;
    }

    public void stopWorking(){
        synchronized (messages) {
            //stop threads and put object state to "stop working"
            sWorking = false;
            threadMM.interrupt();
        }
    }


    /**
     * Constructor that start the thread
     */
    public AbsActor(){
        sWorking = true;
        threadMM = new MessagesManager();
        threadMM.start();
    }

    /**
     * Class that take messages from queue and do the jobs
     */
    private class MessagesManager extends Thread{
        @Override
        public void run() {
            T msg;
            while (!this.isInterrupted() || messages.size() > 0){
                    try {
                        msg = removeMessage();
                        receive(msg);
                    } catch (InterruptedException e) {
                        //System.out.println("Interrotto TH: " + this.getName());
                    }
            }
        }
    }
}



