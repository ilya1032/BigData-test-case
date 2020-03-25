import java.util.TimerTask;

public class UpdateLimitsTask extends TimerTask {

    @Override
    public void run() {

        Limits.getInstance().updateLimits();

    }
}
