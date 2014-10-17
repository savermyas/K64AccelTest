package I2CExample;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import jdk.dio.DeviceDescriptor;
import jdk.dio.DeviceManager;
import jdk.dio.i2cbus.I2CDevice;
import jdk.dio.i2cbus.I2CDeviceConfig;

/**
 *
 * @author Dmitry Myasnikov <dmitry.myasnikov@oracle.com>
 */
public class K64Test extends MIDlet {

    boolean isStopped = false;
    boolean positionChanged = false;
    byte threshold = 5;
    final long timeout = 500;

    final byte FXOS8700Q_STATUS = 0x00;
    final byte FXOS8700Q_OUT_X_MSB = 0x01;
    final byte FXOS8700Q_OUT_Y_MSB = 0x03;
    final byte FXOS8700Q_OUT_Z_MSB = 0x05;
    final byte FXOS8700Q_M_OUT_X_MSB = 0x33;
    final byte FXOS8700Q_M_OUT_Y_MSB = 0x35;
    final byte FXOS8700Q_M_OUT_Z_MSB = 0x37;
    final byte FXOS8700Q_WHOAMI = 0x0D;
    final byte FXOS8700Q_XYZ_DATA_CFG = 0x0E;
    final byte FXOS8700Q_CTRL_REG1 = 0x2A;
    final byte FXOS8700Q_M_CTRL_REG1 = 0x5B;
    final byte FXOS8700Q_M_CTRL_REG2 = 0x5C;
     
    final byte[] accelRegisters = {FXOS8700Q_OUT_X_MSB, FXOS8700Q_OUT_Y_MSB, FXOS8700Q_OUT_Z_MSB, FXOS8700Q_M_OUT_X_MSB, FXOS8700Q_M_OUT_Y_MSB, FXOS8700Q_M_OUT_Z_MSB};
    final byte[] accelValues = new byte[6];
    // byte reg0x01, reg0x03, reg0x05, reg0x33, reg0x35, reg0x37;
  
    @Override
    protected void destroyApp(boolean bln) throws MIDletStateChangeException {
        isStopped = true;
    }

    @Override
    protected void startApp() throws MIDletStateChangeException {
        I2CDevice accel = null;
        try {
            accel = DeviceManager.open(300, I2CDevice.class); //  ID from document http://docs.oracle.com/javame/8.1/get-started-freescale-k64/dio-devices.htm
            DeviceDescriptor dd = accel.getDescriptor();
            I2CDeviceConfig dc = (I2CDeviceConfig) dd.getConfiguration();

            System.out.println("Name: " + dd.getName() + "; Freq: " + dc.getClockFrequency() + "; Address: " + dc.getAddress());
            int rezult = 0;
            ByteBuffer bb = ByteBuffer.allocateDirect(1);

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

            while (!isStopped) {
                
                for (int i = 0; i < accelRegisters.length; i++)
                {
                    positionChanged = false;
                    accel.begin();   //System.out.print("Reading... "); 
                    accel.read(accelRegisters[i], 1, bb);
                    accel.end();
                   
                    if( java.lang.Math.abs(accelValues[i]-bb.get(0))>threshold)
                    {
                       positionChanged = true;
                       System.out.print(accelValues[i]+": "+ bb.get(0));
                    }
                    
                    accelValues[i]=bb.get(0);
                    bb.rewind();
                }
                System.out.println(" Lol");

                /*accel.begin();   //System.out.print("Reading... "); 
                accel.read(0x01, 1, bb);
                accel.end();
                reg0x01 = bb.get(0);
                //System.out.print("0x01: " + bb.get(0));
                bb.rewind();

                accel.begin();   //System.out.print("Reading... "); 
                accel.read(0x03, 1, bb);
                accel.end();
                System.out.print("; 0x03: " + bb.get(0));
                bb.rewind();

                accel.begin();   //System.out.print("Reading... "); 
                accel.read(0x05, 1, bb);
                accel.end();
                System.out.print("; 0x05: " + bb.get(0));
                bb.rewind();

                accel.begin();   //System.out.print("Reading... "); 
                accel.read(0x33, 1, bb);
                accel.end();
                System.out.print("; 0x33: " + bb.get(0));
                bb.rewind();

                accel.begin();   //System.out.print("Reading... "); 
                accel.read(0x35, 1, bb);
                accel.end();
                System.out.print("; 0x35: " + bb.get(0));
                bb.rewind();

                accel.begin();   //System.out.print("Reading... "); 
                accel.read(0x37, 1, bb);
                accel.end();
                System.out.println("; 0x37: " + bb.get(0));
                bb.rewind();*/

                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException ex) {
                    Logger.getLogger(K64Test.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(K64Test.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                accel.close();
            } catch (IOException ex) {
                Logger.getLogger(K64Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}