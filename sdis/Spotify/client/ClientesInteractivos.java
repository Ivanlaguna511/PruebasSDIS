package sdis.Spotify.server;
import sdis.Spotify.common.*;
import sdis.utils.*;

import sdis.Spotify.client.unit.*;
import java.io.IOException;

public class Sirviente implements Runnable {
    private final java.net.Socket socket;
    private final MultiMap<String, String> mapa;
    private final java.io.ObjectOutputStream oos;
    private final java.io.ObjectInputStream ois;
    private final int ns;
    private static java.util.concurrent.atomic.AtomicInteger nInstance =
            new java.util.concurrent.atomic.AtomicInteger();

    public Sirviente(java.net.Socket s, MultiMap<String, String> c) throws java.io.IOException {
        this.socket = s;
        this.mapa = c;
        this.ns = nInstance.getAndIncrement();
        this.oos = new java.io.ObjectOutputStream(socket.getOutputStream());
        this.ois = new java.io.ObjectInputStream(socket.getInputStream());
    }  //se invoca en el Servidor, usualmente

    public void run() {
        try {
            System.out.println("----Server waiting client----");
            while (true) {

                String mensaje;  //String multipurpose
                MensajeProtocolo me = (MensajeProtocolo) ois.readObject();
                MensajeProtocolo ms;
                // me y ms: mensajes entrante y saliente
                System.out.println("Sirviente: " + ns + ": [ME: " + me); // depuracion me
                switch (me.getPrimitiva()) {
                    case INFO:
                    case ADDED:
                    case EMPTY:
                    case DELETED:
                    case XAUTH:
                        break;
                    case MEDIA:
                    case ERROR:
                    case NOTAUTH:
                    case READL:
                        mensaje = mapa.pop(me.getIdCola());
                        if (null != mensaje) {
                            ms = new MensajeProtocolo(Primitiva.MEDIA, mensaje);
                            System.out.println("Sirviente: " + ns + ": [ME: " + ms); // depuracion me
                            oos.writeObject(ms);
                    } else {
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                            System.out.println("Sirviente: " + ns + ": [ME: " + ms); // depuracion me
                            oos.writeObject(ms);
                    }
                    break;
                    case DELETEL:
                        if (null != (mensaje = mapa.pop(me.getIdCola()))) {
                            ms = new MensajeProtocolo(Primitiva.DELETED, mensaje);
                            System.out.println("Sirviente: " + ns + ": [ME: " + ms); // depuracion me
                            oos.writeObject(ms);
                        } else {
                            ms = new MensajeProtocolo(Primitiva.EMPTY);
                            System.out.println("Sirviente: " + ns + ": [ME: " + ms); // depuracion me
                            oos.writeObject(ms);
                        }
                        break;
                    case ADD2L:
                        mapa.push(me.getIdCola(), me.getMensaje());
                        synchronized (mapa) {
                            mapa.notify();
                        }  //despierta un sirviente esperando en un bloqueo de "mapa"
                        ms = new MensajeProtocolo(Primitiva.ADDED);
                        System.out.println("Sirviente: " + ns + ": [ME: " + ms); // depuracion me
                        oos.writeObject(ms);
                        break;


                }  //fin del selector segun el mensaje entrante


            }
        }catch(Exception io){

        }
    }
}
