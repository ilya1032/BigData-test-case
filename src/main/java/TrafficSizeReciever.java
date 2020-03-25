import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;
import org.pcap4j.core.PacketListener;
import org.pcap4j.packet.Packet;

public class TrafficSizeReciever extends Receiver<Long> implements PacketListener {

    public TrafficSizeReciever() {
        super(StorageLevel.MEMORY_AND_DISK_2());
    }

    public TrafficSizeReciever(String ip) {
        super(StorageLevel.MEMORY_AND_DISK_2());
    }

    @Override
    public void onStart() {
        new Thread(this::receive).start();
    }

    private void receive() {
        try {

            // Tell the handle to loop using the listener we created
            TrafficHandler.getInstance().getHandle().loop(-1, this);

            while (!isStopped()){}
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Throwable t) {
            restart("Error receiving data", t);
        }
    }

    @Override
    public void onStop() {
    }

    @Override
    public void gotPacket(Packet packet) {
        store((long) packet.length());
    }
}
