package com.liufei.nacos.servlet;

import java.io.IOException;

public class ClientBootstrap extends AbstractBootstrap{

    public static void main(String[] args) throws IOException {
        ClientBootstrap bootstrap = new ClientBootstrap();
        //发起longpolling
        bootstrap.poll();

        System.in.read();
    }
}
