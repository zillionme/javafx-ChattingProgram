package com.chat_fx_program;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {
    public static ExecutorService threadPool;
    //Vector은 동기화 되어있음
    public static Vector<Client> clients = new Vector<>();

    ServerSocket serverSocket;

    // 서버를 구동시켜서 클라이언트의 연결을 기다리는 메서드(서버소켓 열기)
    public void startServer(String IP,int port){
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(IP,port));
        }catch (Exception e) {
            e.printStackTrace();
            if(!serverSocket.isClosed()) {
                stopServer();
            }
            return ;
        }
        // 클라리언트가 접속할 때까지 계속 기다리는 쓰레드 생성
        Runnable thread = new Runnable() {
            @Override
            public void run(){
                while (true){
                    try {
                        Socket socket = serverSocket.accept();
                        clients.add(new Client(socket));  //연결한 소켓을 가진 클라이언트 객체 생성
                        System.out.println("[클라이언트 접속]"
                                + socket.getRemoteSocketAddress()
                                +": "+ Thread.currentThread().getName());
                    } catch(Exception e) {
                        if(!serverSocket.isClosed()) {  //서버소켓에 문제 생긴 경우
                            stopServer();
                        }
                        break;
                    }
                }

            }

        };
        threadPool = Executors.newCachedThreadPool();  //스레드풀 객체 초기화
        threadPool.submit(thread);

    }

    // 서버의 작동을 중지시키는 메서드
    public void stopServer(){
        try {
            //현재 작동중이 모든 소켓을 닫기
            Iterator<Client> iterator = clients.iterator();
            while(iterator.hasNext()) {
                Client client = iterator.next();
                client.socket.close();
                iterator.remove();
            }

            //모든 클라이언트 연결 끊긴 후, 서버 소켓 객체를 닫기
            if (serverSocket !=null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            //쓰레드 풀 종료
            //서버 소켓 -클라이언트 소켓 연결한 채 기다리는 쓰레드 생성. 해당 쓰레드를 쓰레드풀에 등록. 서버소켓 닫으면, 기다리는 스레드 종료
            if(threadPool !=null && !threadPool.isShutdown()) {
                threadPool.shutdown();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    //ui생성 및 실질적 프로그램 동작
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5)); //안쪽 여백 설정

        //채팅 화면
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("나눔고딕",15));
        root.setCenter(textArea);

        //버튼 만들기
        Button toggleButton = new Button("시작하기");
        toggleButton.setMaxWidth(Double.MAX_VALUE); //8바이트 double의 최댓값
        BorderPane.setMargin(toggleButton, new Insets(1,0,0,0)); //
        root.setBottom(toggleButton);

        String IP = "127.0.0.1"; //로컬호스트
        int port = 9876;

        toggleButton.setOnAction(event ->{
            if(toggleButton.getText().equals("시작하기")) {
                startServer(IP, port);
                Platform.runLater(()->{  //
                    String message = String.format("[서버시작]\n",IP, port); //
                    textArea.appendText(message);
                    toggleButton.setText("종료하기");
                });
            } else {
                stopServer();
                Platform.runLater(()-> {
                    String message = String.format("[서버 종료]\n",IP, port); //???????????
                    textArea.appendText(message);
                    toggleButton.setText("시작하기");
                });
            }
        }); //이벤트 핸들러가 들어간다

        Scene scene = new Scene(root,400,400);
        primaryStage.setTitle("[채팅서버]");
        primaryStage.setOnCloseRequest(event -> stopServer());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //프로그램의 진입점
    public static void main(String[] args) {
        launch(args);
    }
}
