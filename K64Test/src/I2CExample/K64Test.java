package I2CExample;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import jdk.dio.DeviceDescriptor;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.i2cbus.I2CDevice;
import jdk.dio.i2cbus.I2CDeviceConfig;

/**
 *
 * @author Dmitry Myasnikov <dmitry.myasnikov@oracle.com>
 */
public class K64Test extends MIDlet {

    static boolean isStopped = false;
    

    final static byte FXOS8700Q_STATUS = 0x00;
    final static byte FXOS8700Q_OUT_X_MSB = 0x01;
    final static byte FXOS8700Q_OUT_Y_MSB = 0x03;
    final static byte FXOS8700Q_OUT_Z_MSB = 0x05;
    final static byte FXOS8700Q_M_OUT_X_MSB = 0x33;
    final static byte FXOS8700Q_M_OUT_Y_MSB = 0x35;
    final static byte FXOS8700Q_M_OUT_Z_MSB = 0x37;
    final static byte FXOS8700Q_WHOAMI = 0x0D;
    final static byte FXOS8700Q_XYZ_DATA_CFG = 0x0E;
    final static byte FXOS8700Q_CTRL_REG1 = 0x2A;
    final static byte FXOS8700Q_M_CTRL_REG1 = 0x5B;
    final static byte FXOS8700Q_M_CTRL_REG2 = 0x5C;

   
    // byte reg0x01, reg0x03, reg0x05, reg0x33, reg0x35, reg0x37;

    @Override
    protected void destroyApp(boolean bln) throws MIDletStateChangeException {
        isStopped = true;
    }

    @Override
    protected void startApp() throws MIDletStateChangeException {
        I2CDevice accel = null;
        GPIOPin led = null;

        try {
            accel = DeviceManager.open(300, I2CDevice.class); //  ID from document http://docs.oracle.com/javame/8.1/get-started-freescale-k64/dio-devices.htm

            //DeviceDescriptor dd = accel.getDescriptor();
            //I2CDeviceConfig dc = (I2CDeviceConfig) dd.getConfiguration();
            //System.out.println("Name: " + dd.getName() + "; Freq: " + dc.getClockFrequency() + "; Address: " + dc.getAddress());
           

            //preparation
            accel.begin();
            accel.write(FXOS8700Q_CTRL_REG1, 1, ByteBuffer.wrap(new byte[]{0x00}));
            accel.write(FXOS8700Q_M_CTRL_REG1, 1, ByteBuffer.wrap(new byte[]{0x1F}));
            accel.write(FXOS8700Q_M_CTRL_REG2, 1, ByteBuffer.wrap(new byte[]{0x20}));
            accel.write(FXOS8700Q_XYZ_DATA_CFG, 1, ByteBuffer.wrap(new byte[]{0x00}));
            accel.write(FXOS8700Q_CTRL_REG1, 1, ByteBuffer.wrap(new byte[]{0x1C}));
            accel.end();

            accel.begin();
            accel.write(FXOS8700Q_CTRL_REG1, 1, ByteBuffer.wrap(new byte[]{0x01}));
            accel.end();
            
            AccelWorkThread t = new AccelWorkThread();
            t.setAccel(accel);
            new Thread(t).start();


        } catch (IOException ex) {
            Logger.getLogger(K64Test.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                accel.close();
                led.close();
            } catch (IOException ex) {
                Logger.getLogger(K64Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
