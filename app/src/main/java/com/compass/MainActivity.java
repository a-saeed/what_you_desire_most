package com.compass;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //the angle between the device direction and the absolute north.
    float azimuth_angle;
    private SensorManager compassSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    TextView tv_degrees;
    ImageView iv_compass;
    private float current_degree = 0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compassSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer= compassSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer= compassSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    //power hungry sensors , resume and pause
    protected void onResume()
    {
        super.onResume();
        compassSensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        compassSensorManager.registerListener(this,magnetometer,SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause()
    {
        super.onPause();
        compassSensorManager.unregisterListener(this);
    }

    float[] accel_read;
    float[] magnetic_read;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        tv_degrees= (TextView) findViewById(R.id.tv_degrees);
        iv_compass= (ImageView) findViewById(R.id.iv_compass);

        //read magnetometer & accelerometer sensors and
        //store output data to accel_read,magnetic_read
        if(sensorEvent.sensor.getType()== Sensor.TYPE_ACCELEROMETER)
            accel_read= sensorEvent.values;
        if(sensorEvent.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD)
            magnetic_read= sensorEvent.values;

        //use readings if available
        if(accel_read!=null && magnetic_read!=null)
        {
            float[] R= new float[9];
            float[] I= new float[9];
            //if true , rotation matrix will be saved in variable R
            boolean successful_read=
                    SensorManager.getRotationMatrix(R,I,accel_read,magnetic_read);
            if(successful_read)
            {
                //orientation of device is extracted and
                //data of 3 dimensions are stored in the orientation array
                float[] orientation= new float[3];
                SensorManager.getOrientation(R,orientation);
                azimuth_angle=orientation[0];
                //convert azimuth angle from radians to degrees
                float degrees= ((azimuth_angle*180f)/3.14f);
                int degreesInt= Math.round(degrees);
                //0x00B0->(°)
                tv_degrees.setText(Integer.toString(degreesInt) +
                        (char) 0x00B0 + " to absolute north.");

                //animation code to rotate the compass image
                //based on the azimuth angle
                RotateAnimation rotate= new
                        RotateAnimation(current_degree,-degreesInt,
                        Animation.RELATIVE_TO_SELF,0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(100);
                rotate.setFillAfter(true);
                iv_compass.startAnimation(rotate);
                current_degree=-degreesInt;
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
