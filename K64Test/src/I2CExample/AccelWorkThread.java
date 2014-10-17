/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package I2CExample;

import static I2CExample.K64Test.isStopped;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.i2cbus.I2CDevice;

/**
 *
 * @author vagrant
 */
public class AccelWorkThread implements Runnable {

    private boolean positionChanged;

    final byte[] accelRegisters = {K64Test.FXOS8700Q_OUT_X_MSB,
        K64Test.FXOS8700Q_OUT_Y_MSB, K64Test.FXOS8700Q_OUT_Z_MSB,
        K64Test.FXOS8700Q_M_OUT_X_MSB, K64Test.FXOS8700Q_M_OUT_Y_MSB, K64Test.FXOS8700Q_M_OUT_Z_MSB};
    final byte[] accelValues = new byte[6];

    private I2CDevice accel = null;
    private GPIOPin led = null;

    byte threshold = 5;
    final long timeout = 500;

    @Override
    public void run() {
         ByteBuffer bb = ByteBuffer.allocateDirect(1);
        mainCycle:
        while (!K64Test.isStopped) {
            try {
                positionChanged = false;
                for (int i = 0; i < accelRegisters.length; i++) {

                    getAccel().begin();   //System.out.print("Reading... "); 

                    getAccel().read(accelRegisters[i], 1, bb);
                    getAccel().end();

                    if (java.lang.Math.abs(accelValues[i] - bb.get(0)) > threshold) {
                        positionChanged = true;

                    } else {
                        // positionChanged = false;
                    }

                    accelValues[i] = bb.get(0);
                    bb.rewind();
                }
                led = DeviceManager.open(3, GPIOPin.class);

                led.setValue(positionChanged);
                led.close();
            } catch (IOException ex) {
                Logger.getLogger(AccelWorkThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
                Logger.getLogger(K64Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /** 
     * @return the accel
     */
    public I2CDevice getAccel() {
        return accel;
    }

    /**
     * @param accel the accel to set
     */
    public void setAccel(I2CDevice accel) {
        this.accel = accel;
    }
}


