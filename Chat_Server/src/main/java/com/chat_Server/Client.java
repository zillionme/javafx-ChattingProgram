package com.chat_Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
        receive();
    }

    //클라이언트로부터 메시지 전달받는 receive 메소드
    public void receive() {
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        //반복적으로 클라이언트로부터 내용 받기 (인풋 스트림 객체= 내용전달 받을 수 있음)
                        InputStream in = socket.getInputStream();
                        byte[] buffer = new byte[512];
                        int length = in.read(buffer); //클라이언트로 부터 in 내용 전달받아서, buffer에 담기. = 담긴 메시지의 크기
                        if (length == -1) throw new IOException();

                        System.out.println("[메시지 수신 성공]"
                                + socket.getRemoteSocketAddress() //현재 접속한 클라이언트의 ip 주소 정보를 출력
                                + ": " + Thread.currentThread().getName() //쓰레드의 고유한 이름값도 출력
                        );
                        String message = new String(buffer, 0, length, "UTF-8"); //메시지라는 문자열 변수에 버퍼에서 전달받은 내용을 담아서 출력
                        //메시지 전달만 받는 게 아니라, 다른 클라이언트들에게 전달도 함
                        for (Client client : Main.clients) {
                            client.send(message);
                        }

                    }
                } catch (Exception e) {
                    try {
                        System.out.println("[메시지 수신 오류]"
                                + socket.getRemoteSocketAddress() // 메시지를 보낸 클라이언트 주소네트워크 주소 정보를 출력
                                + ": " + Thread.currentThread().getName());// 해당스레드 고유 이름
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        };

        Main.threadPool.submit(thread);

    }

    // 클라이언트에게 메시지 전송하는 send 메소드
    public void send(String message) {
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream out = socket.getOutputStream();
                    byte[] buffer = message.getBytes();
                    //아웃 스트림에 버퍼의 바이트들을 하나씩 쓰기
                    out.write(buffer);
                    out.flush();  // 성공적으로 전송했음

                } catch (Exception e) {
                    try {//송신오류 발생
                        System.out.println("[메시지 전송 오류]"
                                + socket.getRemoteSocketAddress()
                                + Thread.currentThread().getName());
                        //모든 클라이언트 배열에서 현재 존재하는 클라이언트 객체 지워줘야 한다.
                        Main.clients.remove(Client.this);
                        socket.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }

                }
            }

        };
        Main.threadPool.submit(thread);
    }
}
