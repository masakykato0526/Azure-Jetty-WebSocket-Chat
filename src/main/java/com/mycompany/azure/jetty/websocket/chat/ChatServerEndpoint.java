
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/chat-server")
public class ChatServerEndpoint {
    // ロガーの設定
    private final static org.slf4j.Logger logger
            = LoggerFactory.getLogger(ChatServerEndpoint.class);

    // Jetty の実装バグにより今回は接続済みのセッション管理は
    //自身で実装
    private static final Set< Session> sessions
            = Collections.synchronizedSet(new HashSet<Session>());

    @OnOpen
    public void onOpen(Session session) throws IOException {
        logger.info("onOpen: " + session.getId());
        sessions.add(session);
        logger.info("onOpen Number of sessions: " + sessions.size());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        //Jetty 9.1.0.v20131115 では Java SE 8 のコードを記載できないので
        //Java SE 7 でコードを記述 (Lambda 式で書けない) 

        //Jetty の実装バグにより接続済みのセッション管理は自身で実装
        //Set<Session> sessions = session.getOpenSessions();
        //この結果が null を返し NullPointerException が発生
        //おそらく、こちらのバグ
        //https://bugs.eclipse.org/bugs/show_bug.cgi?id=422192
        for (Session sess : sessions) {
            if (sess.isOpen()) {
                sess.getBasicRemote().sendText(message);
            }
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("onClose: " + session.getId());
        sessions.remove(session);
        logger.info("onClose Number of sessions: " + sessions.size());
    }

    @OnError
    public void onError(Throwable p
    ) {
        logger.error("WebSocket onError : ", p);
    }
}
