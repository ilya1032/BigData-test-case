import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;

public final class TrafficHandler {

    private static TrafficHandler instance = null;

    private int snapshotLength = 65536;     // in bytes
    private int readTimeout = 150;          // in milliseconds

    private PcapNetworkInterface device;
    private PcapHandle handle;

    private TrafficHandler() {
        configure();
    }

    private void configure() {

        // Get all network devices and choose one that captures packages across all of them
        try {
            device = Pcaps.findAllDevs().get(1);
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
        if (device == null) {
            System.out.println("No device chosen.");
            System.exit(1);
        }
    }

    public void openHandle() throws PcapNativeException {
        handle = device.openLive(snapshotLength, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, readTimeout);
    }

    public void closeHandle() {
        // Cleanup when complete
        handle.close();
    }

    public static TrafficHandler getInstance() {
        if (instance == null) {
            instance = new TrafficHandler();
        }
        return instance;
    }

    public PcapHandle getHandle() {
        return handle;
    }
}
