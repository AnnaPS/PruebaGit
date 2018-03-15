package websocketserver;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.json.*;

/**
 *
 * @author macuser
 */
public class Server extends org.java_websocket.server.WebSocketServer {

    ArrayList<Mensaje> mensajes;
    ArrayList<Usuario> usuarios;

    public Server(InetSocketAddress address) {
        super(address);
        mensajes = new ArrayList<>();
        usuarios = new ArrayList<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Nuevo cliente en " + conn.getRemoteSocketAddress().getHostName());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Cliente desconectado " + conn.getRemoteSocketAddress());
        //Borrar al usuario de la lista y actualizar a los clientes
        String ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
        for (Usuario u : usuarios) {
            if (u.ip.equals(ip)) {
                usuarios.remove(u);
                break;
            }
        }
        reenviarListaUsuarios();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject json = new JSONObject(message);
            String tipo = json.getString("tipo");
            String nick = json.getString("nick");
            String msj = json.getString("msj");
            Date hora = new Date(json.getLong("hora"));
            if (tipo.equals("handshake")) {
                String ip = conn.getRemoteSocketAddress().getAddress().getHostAddress();
                usuarios.add(new Usuario(ip, nick));
                System.out.println("Conectado " + nick);
                System.out.println("Hay " + usuarios.size() + " usuarios online");
                reenviarListaUsuarios();
            } else {
                mensajes.add(new Mensaje(nick, msj, hora.toString()));
                for (WebSocket ws : super.connections()) {
                    String js = new JSONStringer()
                            .object()
                            .key("nick")
                            .value(nick)
                            .key("msj")
                            .value(msj)
                            .key("hora")
                            .value(new SimpleDateFormat("HH:mm").format(hora))
                            .endObject()
                            .toString();
                    ws.send(js);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        System.out.println("Servidor escuchando en " + super.getAddress().getAddress().getHostAddress());
    }

    private void reenviarListaUsuarios() {
        //Enviamos la lista de usuarios actualizada a todos los usuarios
        String[] lista = new String[usuarios.size()];
        for (int i = 0; i < usuarios.size(); i++) {
            lista[i] = usuarios.get(i).nick;
        }
        for (WebSocket ws : super.connections()) {
            String js = new JSONStringer()
                    .object()
                    .key("tipo")
                    .value("lista")
                    .key("array")
                    .value(lista)
                    .endObject()
                    .toString();
            ws.send(js);
        }
    }
}

hola
