package fr.radi3nt.networking.common.connection;

public interface ConnectionAcceptor<T extends Connection> {

    T accept();
    void close();

}
