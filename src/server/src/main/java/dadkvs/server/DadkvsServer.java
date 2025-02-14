package dadkvs.server;

import static dadkvs.server.Constants.N_LIDER_SERVERS;

import dadkvs.server.comunications.DadkvsConsoleServiceImpl;
import dadkvs.server.comunications.DadkvsMainServiceImpl;
import dadkvs.server.comunications.DadkvsPaxosServiceImpl;
import dadkvs.server.domain.DadkvsServerState;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class DadkvsServer {

    static DadkvsServerState server_state;

    /** Server host port. */
    private static int port;

    public static void main(String[] args) throws Exception {
        final int kvsize = 1000;

        System.out.println(DadkvsServer.class.getSimpleName());

        // Print received arguments.
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // Check arguments.
        if (args.length < 2) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s baseport replica-id%n", Server.class.getName());
            return;
        }

        int base_port = Integer.valueOf(args[0]);
        int my_id = Integer.valueOf(args[1]);
        port = base_port + my_id;

        // Server threads are running in the background.
        System.out.println("Server started wtih port " + port);

        if (my_id < N_LIDER_SERVERS) {
            System.out.println("I am the leader");
        } else {
            System.out.println("I am not the leader");
        }

        System.out.println("----------------------\n");

        server_state = new DadkvsServerState(kvsize, my_id);


        final BindableService service_impl = new DadkvsMainServiceImpl(server_state);
        final BindableService console_impl = new DadkvsConsoleServiceImpl(server_state);
        final BindableService paxos_impl = new DadkvsPaxosServiceImpl(server_state);

        // Create a new server to listen on port.
        Server server = ServerBuilder
                    .forPort(port)
                    .addService(service_impl)
                    .addService(console_impl)
                    .addService(paxos_impl)
                    .build();

        // Start the server.
        server.start();

        // Do not exit the main thread. Wait until server is terminated.
        server.awaitTermination();
    }
}
